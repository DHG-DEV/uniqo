package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Phase 1: Safety & Support, Legal wired to real screens.
 * Phase 2: Account section wired to real screens + real logout.
 * Phase 3: Notifications, Appearance, Language, Location Preferences wired.
 * Remaining Privacy & Security section stays visibly disabled until its phase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val themeMode by ThemePreferences.themeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
    val language by LanguagePreferences.language(context).collectAsState(initial = "en")

    val appearanceSubtitle = when (themeMode) {
        ThemeMode.SYSTEM -> "System Default"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }
    val languageSubtitle = if (language == "en") "English" else language

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SettingsSection("ACCOUNT") {
                SettingsRow("Account Information", Icons.Default.Person, "Manage your account details", onClick = { onNavigate(Routes.ACCOUNT_INFO) })
                SettingsRow("Email & Phone", Icons.Default.Email, "Manage your contact information", onClick = { onNavigate(Routes.EMAIL_PHONE) })
                SettingsRow("Change Password", Icons.Default.Lock, "Update your password", onClick = { onNavigate(Routes.CHANGE_PASSWORD) })
                SettingsRow("Delete Account", Icons.Default.DeleteForever, "Permanently delete your account", destructive = true, onClick = { onNavigate(Routes.DELETE_ACCOUNT) })
                SettingsRow("Log Out", Icons.Default.ExitToApp, destructive = true, onClick = { showLogoutDialog = true }, showDivider = false)
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection("NOTIFICATIONS") {
                SettingsRow("Notification Settings", Icons.Default.Notifications, "Messages, matches, marketplace & more", onClick = { onNavigate(Routes.NOTIFICATION_SETTINGS) }, showDivider = false)
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection("PRIVACY & SECURITY") {
                SettingsRow("Profile Visibility", Icons.Default.Visibility, "Who can see your profile", enabled = false)
                SettingsRow("Location Privacy", Icons.Default.LocationOn, "Control how your location is shared", enabled = false)
                SettingsRow("Who Can Message Me", Icons.Default.Chat, "Control who can start a chat", enabled = false)
                SettingsRow("Blocked Users", Icons.Default.Block, "Manage blocked accounts", enabled = false, showDivider = false)
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection("APP PREFERENCES") {
                SettingsRow("Location Preferences", Icons.Default.MyLocation, "Preferred areas & search radius", onClick = { onNavigate(Routes.LOCATION_PREFERENCES) })
                SettingsRow("Language", Icons.Default.Language, languageSubtitle, onClick = { onNavigate(Routes.LANGUAGE) })
                SettingsRow("Appearance", Icons.Default.DarkMode, appearanceSubtitle, onClick = { onNavigate(Routes.APPEARANCE) }, showDivider = false)
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection("SAFETY & SUPPORT") {
                SettingsRow("Safety Center", Icons.Default.Shield, "Tips for staying safe on Uniqo", onClick = { onNavigate(Routes.SAFETY_CENTER) })
                SettingsRow("Community Guidelines", Icons.Default.Groups, "Our rules for a respectful community", onClick = { onNavigate(Routes.COMMUNITY_GUIDELINES) })
                SettingsRow("Report a Problem", Icons.Default.ReportProblem, "Let us know what went wrong", enabled = false)
                SettingsRow("Help & Support", Icons.Default.HelpOutline, "FAQs and contact support", onClick = { onNavigate(Routes.HELP_SUPPORT) }, showDivider = false)
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection("LEGAL") {
                SettingsRow("Privacy Policy", Icons.Default.PrivacyTip, onClick = { onNavigate(Routes.PRIVACY_POLICY) })
                SettingsRow("Terms & Conditions", Icons.Default.Description, onClick = { onNavigate(Routes.TERMS_CONDITIONS) })
                SettingsRow("Community Guidelines", Icons.Default.Groups, onClick = { onNavigate(Routes.COMMUNITY_GUIDELINES) })
                SettingsRow("Data & Account Deletion", Icons.Default.DeleteForever, onClick = { onNavigate(Routes.DELETE_ACCOUNT) }, showDivider = false)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Log Out", color = Favorite) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardWhite)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tint = when {
                !enabled -> TextSecondary.copy(alpha = 0.4f)
                destructive -> Favorite
                else -> PurplePrimary
            }
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (!enabled) TextSecondary.copy(alpha = 0.5f) else if (destructive) Favorite else TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = if (enabled) 1f else 0.5f)
                    )
                }
            }
            if (!enabled) {
                Text("Soon", style = MaterialTheme.typography.labelSmall, color = TextSecondary.copy(alpha = 0.5f))
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
        if (showDivider) HorizontalDivider(color = Divider, modifier = Modifier.padding(start = 52.dp))
    }
}