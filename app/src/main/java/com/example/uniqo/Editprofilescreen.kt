package com.example.uniqo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.uniqo.ProfileEdits
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val user by repository.currentUserProfile.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(user.name) }
    var username by remember { mutableStateOf(user.username.removePrefix("@")) }
    var bio by remember { mutableStateOf(user.bio) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var location by remember { mutableStateOf(user.location) }

    // Local preview while a new photo is picked/uploading; falls back to
    // the current avatar. Set to the real uploaded URL once upload succeeds.
    var avatarUrl by remember { mutableStateOf(user.avatarUrl) }
    var uploadingPhoto by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Shown as a full-screen overlay in place of this screen's content when
    // true — same pattern LocationPickerScreen is used with elsewhere in
    // the app (it has no nav route of its own).
    var showLocationPicker by remember { mutableStateOf(false) }

    if (showLocationPicker) {
        LocationPickerScreen(
            initialLocation = null,
            onBack = { showLocationPicker = false },
            onConfirm = { geoPoint ->
                location = geoPoint.address ?: ""
                showLocationPicker = false
            }
        )
        return
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val supabaseRepo = repository as? SupabaseRepository
        if (supabaseRepo == null) {
            // MockRepository — just preview locally, no real upload backend.
            avatarUrl = uri.toString()
            return@rememberLauncherForActivityResult
        }
        uploadingPhoto = true
        scope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val result = supabaseRepo.uploadProfilePhoto(bytes)
                    result.onSuccess { url -> avatarUrl = url }
                        .onFailure { errorText = "Couldn't upload photo. Please try again." }
                }
            } catch (e: Exception) {
                errorText = "Couldn't upload photo. Please try again."
            } finally {
                uploadingPhoto = false
            }
        }
    }

    fun save() {
        if (name.isBlank()) {
            errorText = "Full name can't be empty"
            return
        }
        saving = true
        errorText = null
        repository.updateProfile(
            ProfileEdits(
                name = name.trim(),
                username = username.trim(),
                bio = bio.trim(),
                email = email.trim().ifBlank { null }?.takeIf { it != user.email }, // only send if actually changed
                phone = phone.trim(),
                location = location.trim(),
                avatarUrl = avatarUrl.takeIf { it != user.avatarUrl }
            )
        )
        saving = false
        onSaved()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    UserAvatar(avatarUrl, 100)
                    if (uploadingPhoto) {
                        Box(
                            Modifier.matchParentSize().clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ProfileBlue)
                            .clickable(enabled = !uploadingPhoto) { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Tap to change photo", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Spacer(Modifier.height(24.dp))

            EditField("Full Name", name) { name = it }
            EditField("Username", username, prefix = "@") { username = it }
            EditField("Bio", bio, singleLine = false) { bio = it }
            EditField("Email", email) { email = it }
            EditField("Phone", phone) { phone = it }

            // Location — opens the real map picker instead of free-typing an
            // address, same as your other screens that collect a location.
            OutlinedTextField(
                value = location,
                onValueChange = {}, // read-only; set via the picker
                readOnly = true,
                label = { Text("Location") },
                trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = ProfileBlue) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clickable { showLocationPicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = TextPrimary,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = TextSecondary,
                    disabledTrailingIconColor = ProfileBlue
                ),
                shape = RoundedCornerShape(14.dp)
            )

            errorText?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { save() },
                enabled = !saving && !uploadingPhoto,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfileBlue)
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    prefix: String? = null,
    singleLine: Boolean = true,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        prefix = prefix?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        shape = RoundedCornerShape(14.dp)
    )
}