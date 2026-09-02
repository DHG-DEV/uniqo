package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val authRepository = AuthRepository()

// ===========================================================================
// 1. Forgot Password — enter username, request OTP
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onOtpRequested: (username: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            AuthIconBadge(icon = Icons.Default.Lock)
            Spacer(Modifier.height(16.dp))
            Text("Forgot Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter your username to receive an OTP by email",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorText = null },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            errorText?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (username.isBlank()) {
                        errorText = "Enter your username"
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        authRepository.requestPasswordReset(username.trim())
                        loading = false
                        onOtpRequested(username.trim())
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Send OTP", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ===========================================================================
// 2. OTP Verification — reused for BOTH password reset and signup
// ===========================================================================

enum class OtpPurpose { PASSWORD_RESET, SIGNUP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    email: String,
    purpose: OtpPurpose,
    // Only needed when purpose == SIGNUP, to claim the authorized_users row:
    signupSerialNumber: String = "",
    signupDob: String = "",
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var secondsLeft by remember { mutableStateOf(45) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft--
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text("Verify OTP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "A code was sent to $email",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) { otp = it.filter { c -> c.isDigit() }; errorText = null } },
                label = { Text("6-digit code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            errorText?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(10.dp))

            if (secondsLeft > 0) {
                Text(
                    "Resend (00:${secondsLeft.toString().padStart(2, '0')})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Text(
                    "Resend OTP",
                    style = MaterialTheme.typography.bodySmall,
                    color = PurplePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = {
                        secondsLeft = 45
                        scope.launch {
                            if (purpose == OtpPurpose.PASSWORD_RESET) {
                                authRepository.requestPasswordReset(email)
                            }
                            // For SIGNUP, resend re-triggers Supabase's signup email
                            // automatically on next createAccount call — a dedicated
                            // "resend confirmation" RPC can be added if needed.
                        }
                    })
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (otp.length != 6) {
                        errorText = "Enter the 6-digit code"
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        val result = when (purpose) {
                            OtpPurpose.PASSWORD_RESET -> authRepository.verifyPasswordResetOtp(email, otp)
                            OtpPurpose.SIGNUP -> authRepository.verifySignupOtp(
                                email = email,
                                otp = otp,
                                serialNumber = signupSerialNumber,
                                dobDdMmYyyy = signupDob
                            )
                        }
                        loading = false
                        when (result) {
                            is AuthResult.Success -> onVerified()
                            is AuthResult.Error -> errorText = result.message
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Verify OTP", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ===========================================================================
// 3. Change Password — after OTP verified, sets the new password
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            AuthIconBadge(icon = Icons.Default.Lock)
            Spacer(Modifier.height(16.dp))
            Text("Change Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; errorText = null },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorText = null },
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            errorText?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    when {
                        newPassword.length < 8 -> errorText = "Password must be at least 8 characters"
                        newPassword != confirmPassword -> errorText = "Passwords don't match"
                        else -> {
                            loading = true
                            scope.launch {
                                val result = authRepository.changePassword(newPassword)
                                loading = false
                                when (result) {
                                    is AuthResult.Success -> onPasswordChanged()
                                    is AuthResult.Error -> errorText = result.message
                                }
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Change Password", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun AuthIconBadge(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(PurplePrimary, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
    }
}