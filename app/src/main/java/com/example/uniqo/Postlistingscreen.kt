package com.example.uniqo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.io.File

/**
 * A single universal "Create Post" screen. The category picker below covers
 * every kind of thing a student might want to share — physical items
 * (Furniture/Electronics/Books/Appliances), Rooms/PG, or looking for a
 * Roommate. Whatever category is picked, the post goes into the same
 * shared `repository.listings` feed, so it's visible to every user browsing
 * Marketplace (filterable by category there) — one posting flow instead of
 * a separate one per category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListingScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onPosted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var category by remember { mutableStateOf(ListingCategory.FURNITURE) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(ListingCondition.GOOD) }

    // --- Location state ---
    var location by remember { mutableStateOf("") }
    var pickedLatitude by remember { mutableStateOf<Double?>(null) }
    var pickedLongitude by remember { mutableStateOf<Double?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    var imageUris by remember { mutableStateOf(listOf<Uri>()) }
    var errors by remember { mutableStateOf(emptySet<String>()) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun validate(): Boolean {
        val missing = mutableSetOf<String>()
        if (title.isBlank()) missing += "title"
        if (description.isBlank()) missing += "description"
        if (price.toIntOrNull() == null) missing += "price"
        if (location.isBlank()) missing += "location"
        errors = missing
        return missing.isEmpty()
    }

    // --- Location plumbing ---

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.any { it }
        if (granted) {
            isFetchingLocation = true
            coroutineScope.launch {
                val point = getCurrentLocation(context)
                isFetchingLocation = false
                if (point != null) {
                    pickedLatitude = point.latitude
                    pickedLongitude = point.longitude
                    location = point.address ?: "Pinned location"
                } else {
                    Toast.makeText(context, "Couldn't get your location. Try again or pick on map.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission is needed for this.", Toast.LENGTH_SHORT).show()
        }
    }

    fun useCurrentLocation() {
        if (hasLocationPermission(context)) {
            isFetchingLocation = true
            coroutineScope.launch {
                val point = getCurrentLocation(context)
                isFetchingLocation = false
                if (point != null) {
                    pickedLatitude = point.latitude
                    pickedLongitude = point.longitude
                    location = point.address ?: "Pinned location"
                } else {
                    Toast.makeText(context, "Couldn't get your location. Try again or pick on map.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // --- Camera / Gallery plumbing ---

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { imageUris = imageUris + it }
        }
        pendingCameraUri = null
    }

    fun launchCamera() {
        val uri = createCameraImageUri(context)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is needed to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUris = imageUris + it }
    }

    if (showPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPickerDialog = false },
            title = { Text("Add Photo") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("📷  Camera") }

                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🖼  Gallery") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPickerDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Full-screen swap: while picking on the map, replace this screen's content entirely.
    if (showMapPicker) {
        LocationPickerScreen(
            initialLocation = if (pickedLatitude != null && pickedLongitude != null)
                LatLng(pickedLatitude!!, pickedLongitude!!) else null,
            onBack = { showMapPicker = false },
            onConfirm = { point ->
                pickedLatitude = point.latitude
                pickedLongitude = point.longitude
                location = point.address ?: "Pinned location"
                showMapPicker = false
            }
        )
        return
    }

    // SPACING FIX: replaced the default TopAppBar (which reserves a tall fixed
    // height + built-in vertical centering/padding) with a compact custom row.
    // Scaffold is dropped entirely — Scaffold's contentPadding + the TopAppBar's
    // own insets were what created the empty space above "Create a Post".
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(4.dp))
            Text("Create a Post", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "What would you like to post?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(10.dp))
            Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ListingCategory.values().toList()) { cat ->
                    FilterChip(label = cat.label, selected = category == cat, onClick = { category = cat })
                }
            }

            Spacer(Modifier.height(18.dp))
            FormField(
                label = "Title",
                value = title,
                onChange = { title = it },
                isError = errors.contains("title"),
                placeholder = when (category) {
                    ListingCategory.ROOMS -> "e.g. Single Room in 2BHK"
                    ListingCategory.ROOMMATE -> "e.g. Looking for a roommate"
                    else -> "e.g. Wooden Study Table"
                }
            )
            FormField(
                label = "Description",
                value = description,
                onChange = { description = it },
                isError = errors.contains("description"),
                singleLine = false,
                minLines = 3,
                placeholder = "Add the details other students would want to know."
            )
            FormField(
                label = if (category == ListingCategory.ROOMS) "Rent per month (₹)" else "Price (₹)",
                value = price,
                onChange = { price = it.filter(Char::isDigit) },
                isError = errors.contains("price"),
                placeholder = "Enter amount"
            )

            // --- Location field + the two options ---
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                Text("Location", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("Hostel block / area", color = TextSecondary) },
                    isError = errors.contains("location"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                if (pickedLatitude != null && pickedLongitude != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "📍 Pinned (${"%.4f".format(pickedLatitude)}, ${"%.4f".format(pickedLongitude)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { useCurrentLocation() },
                        enabled = !isFetchingLocation,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isFetchingLocation) "Locating..." else "Current location")
                    }
                    OutlinedButton(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick on map")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Condition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ListingCondition.values().forEach { cond ->
                    FilterChip(label = cond.label, selected = condition == cond, onClick = { condition = cond })
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("Add Photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(imageUris) { idx, uri ->
                    Box(modifier = Modifier.size(72.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        )
                        IconButton(
                            onClick = {
                                imageUris = imageUris.toMutableList().also { it.removeAt(idx) }
                            },
                            modifier = Modifier.align(Alignment.TopEnd).size(22.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = CardWhite)
                        ) { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp)) }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(PurpleLight, RoundedCornerShape(14.dp))
                            .clickable { if (imageUris.size < 6) showPickerDialog = true },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, contentDescription = "Add photo", tint = PurplePrimary) }
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    if (validate() && !isPublishing) {
                        isPublishing = true
                        coroutineScope.launch {
                            val imageBytes = imageUris.firstOrNull()?.let { readBytesFromUri(context, it) }
                            val listing = Listing(
                                id = "l${System.currentTimeMillis()}",
                                title = title,
                                price = price.toInt(),
                                category = category,
                                subCategory = category.label,
                                condition = condition,
                                distanceKm = 0.0, // no longer used — live distance is computed on-device when browsing
                                imageRes = 0,
                                description = description,
                                postedDaysAgo = 0,
                                seller = repository.currentUser(),
                                latitude = pickedLatitude,
                                longitude = pickedLongitude,
                                address = location.ifBlank { null }
                            )
                            val supabaseRepo = repository as? SupabaseRepository
                            if (supabaseRepo != null) {
                                // ADD-ON: actually check the result instead of discarding it.
                                // Same call as before — only the handling of its outcome changed.
                                val result = supabaseRepo.uploadListingPhotoAndPublish(listing, imageBytes)
                                isPublishing = false
                                if (result.isSuccess) {
                                    onPosted()
                                } else {
                                    val reason = result.exceptionOrNull()?.message ?: "Unknown error"
                                    Toast.makeText(
                                        context,
                                        "Couldn't publish your post: $reason",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                repository.addListing(listing)
                                isPublishing = false
                                onPosted()
                            }
                        }
                    }
                },
                enabled = !isPublishing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) { Text(if (isPublishing) "Publishing..." else "Publish Post") }

            if (errors.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Please fill in all required fields.", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    isError: Boolean,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = TextSecondary) },
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

/** Creates a fresh content:// URI (via FileProvider) for the camera to write a full-res photo into. */
private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("post_${System.currentTimeMillis()}_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

/** Reads raw bytes from either a gallery content:// URI or a camera FileProvider URI. */
private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? =
    try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }