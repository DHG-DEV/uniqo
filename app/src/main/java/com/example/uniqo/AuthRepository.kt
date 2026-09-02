package com.example.uniqo

import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ===========================================================================
// Result types the UI layer switches on. Deliberately narrow — screens don't
// need exception details, just "which state to show".
// ===========================================================================

sealed class VerifyDetailsResult {
    object Matched : VerifyDetailsResult()
    object NotMatched : VerifyDetailsResult()
    data class Error(val message: String) : VerifyDetailsResult()
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class UsernameCheckResult {
    object Available : UsernameCheckResult()
    object Taken : UsernameCheckResult()
    data class Error(val message: String) : UsernameCheckResult()
}

private val dobFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/**
 * Real auth flows for login, forgot-password, and the serial+DOB-gated
 * signup, all on top of Supabase Auth (auth.users) — no hand-rolled
 * password storage. OTP is Supabase's built-in email OTP.
 *
 * IMPORTANT — Supabase Dashboard setup this depends on:
 *   1. Authentication → Providers → Email: enabled, "Confirm email" ON.
 *   2. Authentication → Email Templates → "Confirm signup" and
 *      "Reset password": both templates must reference {{ .Token }}
 *      (the 6-digit OTP code), not just {{ .ConfirmationURL }}, or the
 *      user won't have a code to type into the app. Supabase's default
 *      templates already include {{ .Token }} — just don't delete it.
 *   3. auth_schema.sql has been run (verify_authorized_user,
 *      claim_authorized_user, username_to_email, is_username_taken).
 */
class AuthRepository {

    private val auth get() = SupabaseClient.client.auth
    private val db get() = SupabaseClient.client.postgrest

    // -----------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------

    /**
     * Resolves username → email server-side (via RPC, so the client never
     * sees auth.users directly), then signs in. On ANY failure — username
     * not found, or found but wrong password — returns the same generic
     * error. Never reveals which one it was.
     */
    suspend fun login(username: String, password: String): AuthResult {
        val generic = AuthResult.Error("Invalid username or password")
        return try {
            val email = resolveUsernameToEmail(username) ?: return generic
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            AuthResult.Success
        } catch (e: Exception) {
            // Covers: RPC returned null (username not found, already handled
            // above), wrong password, unconfirmed email, network errors —
            // all collapse to the same message so nothing is leaked.
            generic
        }
    }

    private suspend fun resolveUsernameToEmail(username: String): String? {
        return try {
            db.rpc("username_to_email", mapOf("p_username" to username))
                .decodeAs<String>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Local session is cleared client-side regardless.
        }
    }

    // -----------------------------------------------------------------
    // Forgot password
    // -----------------------------------------------------------------

    /**
     * Always returns Success from the UI's point of view — whether or not
     * the username exists — so the screen can show "If that account
     * exists, an OTP was sent" without revealing anything. The actual
     * email send only happens if resolution succeeds.
     */
    suspend fun requestPasswordReset(username: String): AuthResult {
        try {
            val email = resolveUsernameToEmail(username)
            if (email != null) {
                auth.resetPasswordForEmail(email)
            }
        } catch (e: Exception) {
            // Swallowed deliberately — see doc comment above.
        }
        return AuthResult.Success
    }

    /**
     * Verifies the OTP the user typed against the email they entered.
     * On success, Supabase Auth establishes a recovery session, which
     * `changePassword` below then uses.
     */
    suspend fun verifyPasswordResetOtp(email: String, otp: String): AuthResult {
        return try {
            auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = email, token = otp)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("That code is incorrect or has expired.")
        }
    }

    /** Requires an active recovery session from verifyPasswordResetOtp above. */
    suspend fun changePassword(newPassword: String): AuthResult {
        return try {
            auth.updateUser {
                password = newPassword
            }
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("Couldn't update your password. Please try again.")
        }
    }

    // -----------------------------------------------------------------
    // New user registration: serial+DOB → create account → email OTP
    // -----------------------------------------------------------------

    /**
     * Step 1 of signup. Expects dob in "dd-MM-yyyy" (matches the format on
     * your college ID cards / the source PDF) and converts it internally.
     */
    suspend fun verifySerialAndDob(serialNumber: String, dobDdMmYyyy: String): VerifyDetailsResult {
        val isoDob = try {
            LocalDate.parse(
                dobDdMmYyyy.trim(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
            ).format(dobFormatter)
        } catch (e: Exception) {
            return VerifyDetailsResult.Error("Enter a valid date as DD-MM-YYYY.")
        }

        return try {
            val matched = db.rpc(
                "verify_authorized_user",
                mapOf("p_serial_number" to serialNumber.trim(), "p_dob" to isoDob)
            ).decodeAs<Boolean>()
            if (matched) VerifyDetailsResult.Matched else VerifyDetailsResult.NotMatched
        } catch (e: Exception) {
            VerifyDetailsResult.Error("Couldn't verify right now. Please try again.")
        }
    }

    suspend fun checkUsernameAvailable(username: String): UsernameCheckResult {
        return try {
            val taken = db.rpc("is_username_taken", mapOf("p_username" to username.trim()))
                .decodeAs<Boolean>()
            if (taken) UsernameCheckResult.Taken else UsernameCheckResult.Available
        } catch (e: Exception) {
            UsernameCheckResult.Error("Couldn't check that right now.")
        }
    }

    /**
     * Step 2 of signup. Creates the Supabase Auth account (this is what
     * triggers Supabase's built-in signup-confirmation email with an OTP
     * code — see the dashboard note in the class doc comment), then
     * creates the profile row. Does NOT yet mark the authorized_users
     * record as registered — that happens in verifySignupOtp, after OTP
     * verification succeeds, so an account that never confirms its email
     * doesn't permanently burn the serial number.
     */
    suspend fun createAccount(
        username: String,
        email: String,
        password: String,
        mobileNumber: String
    ): AuthResult {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = auth.currentUserOrNull()?.id
                ?: return AuthResult.Error("Something went wrong creating your account.")

            db.from("profiles").insert(
                NewProfileRow(
                    id = userId,
                    name = username,
                    username = username,
                    mobileNumber = mobileNumber
                )
            )
            AuthResult.Success
        } catch (e: RestException) {
            if (e.message?.contains("already registered", ignoreCase = true) == true) {
                AuthResult.Error("That email is already in use.")
            } else {
                AuthResult.Error("Couldn't create your account. Please try again.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Couldn't create your account. Please try again.")
        }
    }

    /**
     * Step 3 of signup. Verifies the emailed OTP, which confirms the
     * Supabase Auth account, then atomically claims the authorized_users
     * row via RPC (re-checked server-side — never trust the earlier
     * client-side check alone).
     */
    suspend fun verifySignupOtp(
        email: String,
        otp: String,
        serialNumber: String,
        dobDdMmYyyy: String
    ): AuthResult {
        return try {
            auth.verifyEmailOtp(type = OtpType.Email.SIGNUP, email = email, token = otp)
            val userId = auth.currentUserOrNull()?.id
                ?: return AuthResult.Error("Verification succeeded but session is missing. Please log in.")

            val isoDob = LocalDate.parse(
                dobDdMmYyyy.trim(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
            ).format(dobFormatter)

            val claimedId = db.rpc(
                "claim_authorized_user",
                mapOf(
                    "p_serial_number" to serialNumber.trim(),
                    "p_dob" to isoDob,
                    "p_user_id" to userId
                )
            ).decodeAs<String>()

            db.from("profiles").update(
                mapOf("authorized_user_id" to claimedId)
            ) { filter { eq("id", userId) } }

            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("That code is incorrect, has expired, or this serial number was already claimed.")
        }
    }
}

@Serializable
private data class NewProfileRow(
    val id: String,
    val name: String,
    val username: String,
    @SerialName("mobile_number") val mobileNumber: String
)