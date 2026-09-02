package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/* -------------------------------------------------------------------------- */
/* SHARED LOCAL HELPERS                                                       */
/* -------------------------------------------------------------------------- */

@Composable
private fun AccountCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun AccountInfoRow(label: String, value: String, showDivider: Boolean = true) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
        if (showDivider) HorizontalDivider(color = Divider)
    }
}

@Composable
private fun VerificationChip(verified: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (verified) SuccessGreen.copy(alpha = 0.12f) else TextSecondary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (verified) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (verified) SuccessGreen else TextSecondary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(if (verified) "Verified" else "Unverified", style = MaterialTheme.typography.labelSmall, color = if (verified) SuccessGreen else TextSecondary)
    }
}

/* -------------------------------------------------------------------------- */
/* ACCOUNT INFORMATION                                                        */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInformationScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    val user by repository.currentUserProfile.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Account Information") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AccountCard {
                AccountInfoRow("Full Name", user.name)
                AccountInfoRow("Username", if (user.username.isNotBlank()) "@${user.username.removePrefix("@")}" else "Not set")
                AccountInfoRow("Bio", user.bio.ifBlank { "Not set" })
                AccountInfoRow("College", user.college.ifBlank { "Not set" })
                AccountInfoRow(
                    "Course / Year",
                    listOf(user.course, user.year).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Not set" },
                    showDivider = false
                )
            }

            Spacer(Modifier.height(16.dp))

            AccountCard {
                AccountInfoRow("Member Since", user.memberSince.ifBlank { "—" })
                AccountInfoRow("Rating", "%.1f".format(user.rating), showDivider = false)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* -------------------------------------------------------------------------- */
/* EMAIL & PHONE                                                              */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailPhoneScreen(
    repository: UniqoRepository,
    onBack: () -> Unit
) {
    val user by repository.currentUserProfile.collectAsState()
    var email by remember(user.email) { mutableStateOf(user.email) }
    var phone by remember(user.phone) { mutableStateOf(user.phone) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Email & Phone") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AccountCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Email", fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
                    VerificationChip(user.verification.emailVerified)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; successText = null },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            AccountCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Phone", fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
                    VerificationChip(user.verification.phoneVerified)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; successText = null },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            errorText?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            successText?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    errorText = null
                    successText = null
                    if (email.isBlank()) {
                        errorText = "Email can't be empty"
                        return@Button
                    }
                    repository.updateProfile(
                        ProfileEdits(
                            email = email.trim().takeIf { it != user.email },
                            phone = phone.trim().takeIf { it != user.phone }
                        )
                    )
                    successText = "Changes saved"
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Changing your email may require re-verifying it before it takes effect.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* -------------------------------------------------------------------------- */
/* CHANGE PASSWORD                                                            */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AccountCard {
                PasswordField("Current Password", currentPassword, showCurrent, { currentPassword = it }, { showCurrent = !showCurrent }, showDivider = false)
            }

            Spacer(Modifier.height(16.dp))

            AccountCard {
                PasswordField("New Password", newPassword, showNew, { newPassword = it }, { showNew = !showNew })
                Spacer(Modifier.height(12.dp))
                PasswordField("Confirm New Password", confirmPassword, showNew, { confirmPassword = it }, { showNew = !showNew }, showDivider = false)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Use at least 8 characters, mixing letters and numbers.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            errorText?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            successText?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    errorText = null
                    successText = null
                    when {
                        currentPassword.isBlank() || newPassword.isBlank() -> errorText = "Please fill in all fields"
                        newPassword.length < 8 -> errorText = "New password must be at least 8 characters"
                        newPassword != confirmPassword -> errorText = "Passwords don't match"
                        else -> {
                            loading = true
                            scope.launch {
                                val result = AuthManager.updatePassword(currentPassword, newPassword)
                                loading = false
                                result.onSuccess {
                                    successText = "Password updated successfully"
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }.onFailure {
                                    errorText = it.message ?: "Couldn't update password. Check your current password and try again."
                                }
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Update Password", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    showDivider: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle visibility", tint = TextSecondary)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

/* -------------------------------------------------------------------------- */
/* DELETE ACCOUNT                                                             */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var confirmText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Delete Account") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AccountCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Favorite, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Deleting your account is permanent and cannot be undone. This will remove your profile, listings, roommate preferences, and other data associated with your account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Type DELETE to confirm", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmText,
                onValueChange = { confirmText = it; errorText = null },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            errorText?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (confirmText.trim() != "DELETE") {
                        errorText = "Please type DELETE exactly to confirm"
                        return@Button
                    }
                    loading = true
                    errorText = null
                    scope.launch {
                        val result = AuthManager.deleteAccount()
                        loading = false
                        result.onSuccess { onAccountDeleted() }
                            .onFailure { errorText = it.message ?: "Couldn't delete your account. Please try again." }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Favorite)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Permanently Delete Account", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}