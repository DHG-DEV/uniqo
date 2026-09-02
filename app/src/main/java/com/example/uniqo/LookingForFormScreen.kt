package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookingForFormScreen(
    category: LookingForCategory,
    existing: LookingForPreference?,
    repository: UniqoRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val specs = remember(category) { LookingForFieldSpecs.forCategory(category) }
    var fields by remember { mutableStateOf(existing?.fields ?: emptyMap()) }
    var openFieldKey by remember { mutableStateOf<String?>(null) }
    var deleteConfirmVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showMapPicker by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val openSpec = specs.firstOrNull { it.key == openFieldKey }

    // Full-screen swap: while picking on the map, replace this screen's content entirely.
    if (showMapPicker) {
        val currentLatLng = fields["exactLocation"]
            ?.substringBefore("|", missingDelimiterValue = "")
            ?.split(",")
        val initialLocation = if (currentLatLng != null && currentLatLng.size == 2) {
            LatLng(
                currentLatLng[0].toDoubleOrNull() ?: 0.0,
                currentLatLng[1].toDoubleOrNull() ?: 0.0
            )
        } else null

        LocationPickerScreen(
            initialLocation = initialLocation,
            onBack = { showMapPicker = false },
            onConfirm = { point ->
                fields = fields + ("exactLocation" to "${point.latitude},${point.longitude}|${point.address ?: "Pinned location"}")
                showMapPicker = false
            }
        )
        return
    }

    if (openSpec != null && openSpec.type == FieldType.LOCATION) {
        LocationFieldOptionsDialog(
            onDismiss = { openFieldKey = null },
            onUseCurrentLocation = {
                openFieldKey = null
                isFetchingLocation = true
                coroutineScope.launch {
                    val point = getCurrentLocation(context)
                    isFetchingLocation = false
                    if (point != null) {
                        fields = fields + ("exactLocation" to "${point.latitude},${point.longitude}|${point.address ?: "Pinned location"}")
                    }
                }
            },
            onPickOnMap = {
                openFieldKey = null
                showMapPicker = true
            }
        )
    } else if (openSpec != null) {
        FieldEditorSheet(
            spec = openSpec,
            currentValue = fields[openSpec.key] ?: "",
            onDismiss = { openFieldKey = null },
            onSave = { newValue ->
                fields = fields + (openSpec.key to newValue)
                openFieldKey = null
            }
        )
    }

    if (deleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { deleteConfirmVisible = false },
            title = { Text("Remove this preference?") },
            text = { Text("This will stop showing on your Home screen.") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let { repository.deactivateLookingForPreference(it.id) }
                    deleteConfirmVisible = false
                    onSaved()
                }) { Text("Remove", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmVisible = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("${category.icon} ${category.label}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    if (existing != null) {
                        TextButton(onClick = { deleteConfirmVisible = true }) {
                            Text("Remove", color = ErrorRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
                Button(
                    onClick = {
                        if (existing != null) {
                            repository.updateLookingForPreference(existing.id, fields)
                        } else {
                            repository.addLookingForPreference(category, fields)
                        }
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) { Text(if (existing != null) "Save Changes" else "Save Preference") }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(specs, key = { it.key }) { spec ->
                val rawValue = fields[spec.key].orEmpty()
                val displayValue = if (spec.type == FieldType.LOCATION) {
                    rawValue.substringAfter("|", missingDelimiterValue = "")
                } else rawValue

                PreferenceRow(
                    spec = spec,
                    value = if (spec.type == FieldType.LOCATION && isFetchingLocation) "Locating..." else displayValue,
                    onClick = { openFieldKey = spec.key }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PreferenceRow(spec: FieldSpec, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(spec.label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value.ifBlank { "Any" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isBlank()) TextSecondary else PurplePrimary
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun LocationFieldOptionsDialog(
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onPickOnMap: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Exact Location") },
        text = {
            Column {
                TextButton(onClick = onUseCurrentLocation, modifier = Modifier.fillMaxWidth()) {
                    Text("📍  Use my current location")
                }
                TextButton(onClick = onPickOnMap, modifier = Modifier.fillMaxWidth()) {
                    Text("🗺️  Pick on map")
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldEditorSheet(
    spec: FieldSpec,
    currentValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(spec.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            when (spec.type) {
                FieldType.CHIPS_SINGLE -> {
                    var selected by remember { mutableStateOf(currentValue) }
                    FlowRowChips(options = spec.options, selected = setOf(selected)) { option ->
                        selected = option
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { onSave(selected) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) { Text("Done") }
                }

                FieldType.CHIPS_MULTI -> {
                    var selected by remember {
                        mutableStateOf(currentValue.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet())
                    }
                    FlowRowChips(options = spec.options, selected = selected) { option ->
                        selected = if (selected.contains(option)) selected - option else selected + option
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { onSave(selected.joinToString(", ")) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) { Text("Done") }
                }

                FieldType.BUDGET_RANGE -> {
                    val parts = currentValue.split("-")
                    var min by remember { mutableStateOf(parts.getOrNull(0)?.trim() ?: "") }
                    var max by remember { mutableStateOf(parts.getOrNull(1)?.trim() ?: "") }
                    OutlinedTextField(
                        value = min, onValueChange = { min = it.filter(Char::isDigit) },
                        label = { Text("Minimum (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = max, onValueChange = { max = it.filter(Char::isDigit) },
                        label = { Text("Maximum (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { onSave("$min - $max") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) { Text("Done") }
                }

                FieldType.AREAS_MULTI, FieldType.TEXT -> {
                    var text by remember { mutableStateOf(currentValue) }
                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        placeholder = {
                            Text(if (spec.type == FieldType.AREAS_MULTI) "e.g. Koramangala, Indiranagar" else "Enter ${spec.label.lowercase()}")
                        },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { onSave(text) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) { Text("Done") }
                }

                FieldType.LOCATION -> {
                    // Handled entirely by LocationFieldOptionsDialog before this sheet
                    // ever opens — this branch never actually renders, but is
                    // required for the `when` to be exhaustive.
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FlowRowChips(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    FilterChip(label = option, selected = selected.contains(option), onClick = { onToggle(option) })
                }
            }
        }
    }
}