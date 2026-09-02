
package com.example.uniqo

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SignOutScope
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

private const val FUNCTIONS_BASE =
    "https://skpyrgqtyfypqtoinvpw.supabase.co/functions/v1"

private const val ANON_KEY =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrcHlyZ3F0eWZ5cHF0b2ludnB3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MzMxNzQsImV4cCI6MjEwMzEwOTE3NH0.XbBn44BZDDcLNNO5hGa1ttDIlNiOkT-rZTN5PLuuT_Q"

@Serializable
data class AuthError(val error: String)

@Serializable
private data class VerifyAuthorizedRequest(
    val serialNumber: String,
    val dob: String
)

@Serializable
data class VerifyAuthorizedResponse(
    val verified: Boolean,
    val authorizedUserId: String,
    val verifyToken: String
)

@Serializable
private data class CreateAccountRequest(
    val authorizedUserId: String,
    val verifyToken: String,
    val username: String,
    val password: String,
    val mobileNumber: String
)

@Serializable
data class CreateAccountResponse(
    val userId: String,
    val otpSent: Boolean
)

@Serializable
private data class VerifyOtpRequest(
    val userId: String,
    val code: String
)

@Serializable
data class VerifyOtpResponse(
    val verified: Boolean
)

@Serializable
private data class ForgotPasswordRequest(
    val action: String,
    val username: String? = null,
    val code: String? = null,
    val userId: String? = null,
    val newPassword: String? = null
)

@Serializable
data class ForgotPasswordResponse(
    val message: String? = null,
    val verified: Boolean? = null,
    val resetToken: String? = null,
    val userId: String? = null,
    val success: Boolean? = null
)

@Serializable
private data class DeleteAccountResponse(
    val success: Boolean
)

object AuthManager {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
    }

    private suspend inline fun <reified Req, reified Res> call(
        path: String,
        body: Req
    ): Result<Res> = runCatching {

        val response = client.post("$FUNCTIONS_BASE/$path") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $ANON_KEY")
            setBody(body)
        }

        if (response.status.value >= 400) {
            val err = response.body<AuthError>()
            error(err.error)
        }

        response.body()
    }

    suspend fun verifyAuthorizedUser(
        serialNumber: String,
        dob: String
    ): Result<VerifyAuthorizedResponse> =
        call(
            "verify-authorized-user",
            VerifyAuthorizedRequest(serialNumber, dob)
        )

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        college: String,
        course: String,
        year: String
    ): Result<Unit> = runCatching {

        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val userId =
            SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: error("Signup failed - no user ID returned")

        SupabaseClient.client.postgrest
            .from("profiles")
            .insert(
                mapOf(
                    "id" to userId,
                    "name" to name,
                    "college" to college,
                    "course" to course,
                    "year" to year
                )
            )
    }

    suspend fun createAccount(
        authorizedUserId: String,
        verifyToken: String,
        username: String,
        password: String,
        mobileNumber: String
    ): Result<CreateAccountResponse> =
        call(
            "create-account",
            CreateAccountRequest(
                authorizedUserId,
                verifyToken,
                username,
                password,
                mobileNumber
            )
        )

    suspend fun verifyMobileOtp(
        userId: String,
        code: String
    ): Result<VerifyOtpResponse> =
        call(
            "verify-mobile-otp",
            VerifyOtpRequest(userId, code)
        )

    /**
     * Uses Supabase's built-in email/password sign-in.
     *
     * Username is first converted to the user's email through
     * the existing username_to_email RPC.
     */
    suspend fun signIn(
        username: String,
        password: String
    ): Result<Unit> = runCatching {

        val email = SupabaseClient.client.postgrest
            .rpc(
                "username_to_email",
                mapOf("p_username" to username)
            )
            .decodeAs<String>()

        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun requestPasswordReset(
        username: String
    ): Result<ForgotPasswordResponse> =
        call(
            "forgot-password",
            ForgotPasswordRequest(
                action = "request",
                username = username
            )
        )

    suspend fun verifyPasswordResetOtp(
        username: String,
        code: String
    ): Result<ForgotPasswordResponse> =
        call(
            "forgot-password",
            ForgotPasswordRequest(
                action = "verify",
                username = username,
                code = code
            )
        )

    suspend fun resetPassword(
        userId: String,
        newPassword: String
    ): Result<ForgotPasswordResponse> =
        call(
            "forgot-password",
            ForgotPasswordRequest(
                action = "reset",
                userId = userId,
                newPassword = newPassword
            )
        )

    suspend fun signOut() {
        try {
            SupabaseClient.client.auth.signOut(
                SignOutScope.LOCAL
            )
        } catch (e: Exception) {
            // Best-effort — local state is what matters here.
        }
    }

    /**
     * Updates the current user's password.
     *
     * First signs in again using the current password so that
     * the existing password is verified before changing it.
     */
    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {

        val email =
            SupabaseClient.client.auth.currentUserOrNull()?.email
                ?: error("No active session")

        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = currentPassword
        }

        SupabaseClient.client.auth.updateUser {
            password = newPassword
        }
    }

    /**
     * Permanently deletes the currently authenticated account
     * through the delete-account Supabase Edge Function.
     *
     * The user's access token is sent to the Edge Function so
     * the server can verify the user's identity before deletion.
     */
    suspend fun deleteAccount(): Result<Unit> = runCatching {

        val session =
            SupabaseClient.client.auth.currentSessionOrNull()
                ?: error("No active session")

        val response = client.post(
            "$FUNCTIONS_BASE/delete-account"
        ) {
            contentType(ContentType.Application.Json)
            header(
                "Authorization",
                "Bearer ${session.accessToken}"
            )
            setBody(emptyMap<String, String>())
        }

        if (response.status.value >= 400) {
            val err =
                runCatching {
                    response.body<AuthError>()
                }.getOrNull()

            error(
                err?.error ?: "Account deletion failed"
            )
        }

        signOut()
    }

    fun currentUserId(): String? =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: SupabaseClient.client.auth.currentSessionOrNull()?.user?.id

    suspend fun awaitInitialUserId(): String? {

        Log.d(
            "STARTUP",
            "auth init START ${System.currentTimeMillis()}"
        )

        SupabaseClient.client.auth.awaitInitialization()

        Log.d(
            "STARTUP",
            "auth init END ${System.currentTimeMillis()}"
        )

        return SupabaseClient.client.auth.currentSessionOrNull()
            ?.let { it.user?.id }
            ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }
}
