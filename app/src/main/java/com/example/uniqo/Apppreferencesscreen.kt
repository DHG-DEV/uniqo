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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
private fun PrefCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .padding(4.dp),
        content = content
    )
}

/* -------------------------------------------------------------------------- */
/* APPEARANCE                                                                 */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentMode by ThemePreferences.themeMode(context).collectAsState(initial = ThemeMode.SYSTEM)

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrefCard {
                ThemeOptionRow("System Default", "Match your device setting", ThemeMode.SYSTEM, currentMode) {
                    scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) }
                }
                ThemeOptionRow("Light", "Always use light mode", ThemeMode.LIGHT, currentMode) {
                    scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) }
                }
                ThemeOptionRow("Dark", "Always use dark mode", ThemeMode.DARK, currentMode, showDivider = false) {
                    scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeOptionRow(title: String, subtitle: String, mode: ThemeMode, selected: ThemeMode, showDivider: Boolean = true, onSelect: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            if (mode == selected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            }
        }
        if (showDivider) HorizontalDivider(color = Divider, modifier = Modifier.padding(start = 12.dp))
    }
}

/* -------------------------------------------------------------------------- */
/* LANGUAGE                                                                   */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentLanguage by LanguagePreferences.language(context).collectAsState(initial = "en")

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Language") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrefCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("English", style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                    if (currentLanguage == "en") {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "More languages coming soon.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* -------------------------------------------------------------------------- */
/* NOTIFICATION SETTINGS                                                      */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by NotificationPreferences.settings(context).collectAsState(initial = NotificationSettings())

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrefCard {
                NotifSwitchRow(Icons.Default.Chat, "Messages", "Receive notifications for new messages", settings.messages) {
                    scope.launch { NotificationPreferences.setMessages(context, it) }
                }
                NotifSwitchRow(Icons.Default.Home, "Roommate Matches", "Receive notifications for compatible roommate matches", settings.roommateMatches) {
                    scope.launch { NotificationPreferences.setRoommateMatches(context, it) }
                }
                NotifSwitchRow(Icons.Default.Inventory2, "Marketplace Activity", "Notifications for listing activity", settings.marketplaceActivity) {
                    scope.launch { NotificationPreferences.setMarketplaceActivity(context, it) }
                }
                NotifSwitchRow(Icons.Default.Handshake, "Offers & Requests", "Notifications for offers and requests", settings.offersRequests) {
                    scope.launch { NotificationPreferences.setOffersRequests(context, it) }
                }
                NotifSwitchRow(Icons.Default.Campaign, "General Updates", "Important app updates and announcements", settings.generalUpdates, showDivider = false) {
                    scope.launch { NotificationPreferences.setGeneralUpdates(context, it) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NotifSwitchRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, showDivider: Boolean = true, onToggle: (Boolean) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = PurplePrimary)
            )
        }
        if (showDivider) HorizontalDivider(color = Divider, modifier = Modifier.padding(start = 12.dp))
    }
}

/* -------------------------------------------------------------------------- */
/* LOCATION PREFERENCES                                                       */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPreferencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferredAreas by LocationPrefsStore.preferredAreas(context).collectAsState(initial = emptyList())
    val radiusKm by LocationPrefsStore.searchRadiusKm(context).collectAsState(initial = 10)
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        LocationPickerScreen(
            initialLocation = null,
            onBack = { showPicker = false },
            onConfirm = { geoPoint ->
                val address = geoPoint.address
                if (!address.isNullOrBlank()) {
                    scope.launch { LocationPrefsStore.addPreferredArea(context, address) }
                }
                showPicker = false
            }
        )
        return
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Location Preferences") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text("Preferred Areas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            PrefCard {
                if (preferredAreas.isEmpty()) {
                    Text(
                        "No preferred areas added yet.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    preferredAreas.forEachIndexed { index, area ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(area, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                            IconButton(onClick = { scope.launch { LocationPrefsStore.removePreferredArea(context, area) } }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (index != preferredAreas.lastIndex) HorizontalDivider(color = Divider, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            TextButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Preferred Area", color = PurplePrimary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))

            Text("Search Radius", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            PrefCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$radiusKm km", color = PurplePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = radiusKm.toFloat(),
                        onValueChange = { newVal ->
                            scope.launch { LocationPrefsStore.setSearchRadiusKm(context, newVal.toInt()) }
                        },
                        valueRange = 1f..50f,
                        steps = 48,
                        colors = SliderDefaults.colors(thumbColor = PurplePrimary, activeTrackColor = PurplePrimary)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}