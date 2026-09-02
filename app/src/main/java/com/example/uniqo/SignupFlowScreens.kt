package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Shield
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

private val signupAuthRepository = AuthRepository()

// ===========================================================================
// 1. Verify Your Details — Serial Number + DOB, checked against the PDF-
//    imported authorized_users table via the verify_authorized_user RPC.
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySerialDobScreen(
    onBack: () -> Unit,
    onVerified: (serialNumber: String, dobDdMmYyyy: String) -> Unit
) {
    var serialNumber by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") } // DD-MM-YYYY, matches your ID cards
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Verify Your Details") },
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
            AuthIconBadge(icon = Icons.Default.Shield)
            Spacer(Modifier.height(16.dp))
            Text("Verify Your Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter your Serial Number and Date of Birth exactly as printed on your ID",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = serialNumber,
                onValueChange = { serialNumber = it; errorText = null },
                label = { Text("Serial Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it; errorText = null },
                label = { Text("Date of Birth (DD-MM-YYYY)") },
                singleLine = true,
                placeholder = { Text("18-01-2003") },
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
                    if (serialNumber.isBlank() || dob.isBlank()) {
                        errorText = "Enter both your Serial Number and Date of Birth"
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        when (val result = signupAuthRepository.verifySerialAndDob(serialNumber, dob)) {
                            is VerifyDetailsResult.Matched -> {
                                loading = false
                                onVerified(serialNumber.trim(), dob.trim())
                            }
                            is VerifyDetailsResult.NotMatched -> {
                                loading = false
                                errorText = "The details could not be verified. Please check your information."
                            }
                            is VerifyDetailsResult.Error -> {
                                loading = false
                                errorText = result.message
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Verify", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ===========================================================================
// 2. Create Account — username, password, mobile number, then send OTP
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    serialNumber: String,
    dobDdMmYyyy: String,
    onBack: () -> Unit,
    onOtpSent: (email: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var mobileNumber by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
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
            AuthIconBadge(icon = Icons.Default.PersonAddAlt)
            Spacer(Modifier.height(16.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorText = null },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorText = null },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorText = null },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it; errorText = null },
                label = { Text("Mobile Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                        username.isBlank() -> errorText = "Enter a username"
                        email.isBlank() || !email.contains("@") -> errorText = "Enter a valid email"
                        password.length < 8 -> errorText = "Password must be at least 8 characters"
                        mobileNumber.isBlank() -> errorText = "Enter your mobile number"
                        else -> {
                            loading = true
                            scope.launch {
                                val usernameCheck = signupAuthRepository.checkUsernameAvailable(username)
                                if (usernameCheck is UsernameCheckResult.Taken) {
                                    loading = false
                                    errorText = "That username is already taken"
                                    return@launch
                                }
                                val result = signupAuthRepository.createAccount(
                                    username = username.trim(),
                                    email = email.trim(),
                                    password = password,
                                    mobileNumber = mobileNumber.trim()
                                )
                                loading = false
                                when (result) {
                                    is AuthResult.Success -> onOtpSent(email.trim())
                                    is AuthResult.Error -> errorText = result.message
                                }
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
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
// 3. Registration Success
// ===========================================================================

@Composable
fun RegistrationSuccessScreen(onGoToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SuccessGreen, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Account Created Successfully", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "You can now log in with your credentials",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onGoToLogin,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Go to Login", fontWeight = FontWeight.SemiBold)
        }
    }
}