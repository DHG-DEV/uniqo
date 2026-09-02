package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(onBack: () -> Unit, onSignedUp: () -> Unit) {
    var step by remember { mutableStateOf(0) } // 0 = form, 1 = verify email
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submitSignup() {
        if (name.isBlank() || email.isBlank() || password.isBlank() || college.isBlank() || course.isBlank() || year.isBlank()) {
            errorMessage = "Please fill in every field."
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = AuthManager.signUp(email.trim(), password, name, college, course, year)
            isLoading = false
            result.onSuccess { step = 1 }
                .onFailure { e -> errorMessage = e.message ?: "Sign up failed. Try again." }
        }
    }

    fun confirmVerified() {
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = AuthManager.signIn(email.trim(), password)
            result.onSuccess {
                (RepositoryProvider.repository as SupabaseRepository).onAuthenticated()
                isLoading = false
                onSignedUp()
            }.onFailure {
                isLoading = false
                errorMessage = "Not verified yet — check your inbox and tap the confirmation link first."
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(if (step == 0) "Sign Up" else "Verify Email") },
                navigationIcon = {
                    IconButton(onClick = { if (step == 0) onBack() else step = 0 }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        if (step == 0) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(8.dp))
                LabeledField("Full name", name, enabled = !isLoading) { name = it }
                LabeledField("Email", email, enabled = !isLoading) { email = it }
                LabeledPasswordField("Password", password, enabled = !isLoading) { password = it }
                LabeledField("College", college, enabled = !isLoading) { college = it }
                LabeledField("Course", course, enabled = !isLoading) { course = it }
                LabeledField("Year", year, enabled = !isLoading) { year = it }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { submitSignup() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CardWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Create Account")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(PurpleLight, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("Verify your student email", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "We've sent a verification link to ${email.ifBlank { "your college email" }}. Tap it, then come back here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { confirmVerified() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CardWhite, strokeWidth = 2.dp)
                    } else {
                        Text("I've verified — Continue")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, enabled: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun LabeledPasswordField(label: String, value: String, enabled: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp)
    )
}