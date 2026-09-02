package com.example.uniqo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit, onLoggedIn: () -> Unit, onGoToSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Enter your email and password."
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = AuthManager.signIn(email.trim(), password)
            result.onSuccess {
                (RepositoryProvider.repository as SupabaseRepository).onAuthenticated()
                isLoading = false
                onLoggedIn()
            }.onFailure { e ->
                isLoading = false
                errorMessage = e.message ?: "Login failed. Check your email and password."
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(16.dp))
            Text("Welcome back", style = MaterialTheme.typography.headlineSmall)
            Text("Log in with your student email to continue.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("College email") },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { submit() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CardWhite, strokeWidth = 2.dp)
                } else {
                    Text("Log In")
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("New to Uniqo? ", color = TextSecondary)
                Text(
                    "Sign up",
                    color = PurplePrimary,
                    style = MaterialTheme.typography.bodyLarge.copy(color = PurplePrimary)
                )
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onGoToSignup, modifier = Modifier.fillMaxWidth()) {
                Text("Create a new account", color = PurplePrimary)
            }
        }
    }
}