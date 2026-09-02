package com.example.uniqo

import androidx.compose.runtime.Composable

/**
 * A wrapper around the generic LocationPickerScreen that connects it to the
 * user's profile. When a location is confirmed, it updates the repository
 * and navigates back.
 */
@Composable
fun ProfileLocationPickerScreen(
    repository: UniqoRepository,
    onBack: () -> Unit
) {
    LocationPickerScreen(
        initialLocation = null,
        onBack = onBack,
        onConfirm = { geoPoint ->
            repository.updateProfile(ProfileEdits(location = geoPoint.address ?: ""))
            onBack()
        }
    )
}
