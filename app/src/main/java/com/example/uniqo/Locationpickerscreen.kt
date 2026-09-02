package com.example.uniqo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Full-screen map with a fixed center pin. If no initial location is passed
 * (nothing picked yet), it opens by jumping to the user's current GPS
 * location. The user pans the map to move the pin; "Confirm" reads whatever
 * point is currently under the pin (the camera's target) and reverse-geocodes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    initialLocation: LatLng? = null,
    onBack: () -> Unit,
    onConfirm: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fallback = LatLng(18.5204, 73.8567) // used only if GPS also fails
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation ?: fallback, 15f)
    }
    var isLocatingUser by remember { mutableStateOf(initialLocation == null) }
    var isResolving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialLocation == null) {
            val point = getCurrentLocation(context)
            if (point != null) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(point.latitude, point.longitude), 16f)
                )
            }
            isLocatingUser = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick a location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        isResolving = true
                        val center = cameraPositionState.position.target
                        coroutineScope.launch {
                            val address = reverseGeocode(context, center.latitude, center.longitude)
                            isResolving = false
                            onConfirm(GeoPoint(center.latitude, center.longitude, address))
                        }
                    },
                    enabled = !isResolving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) { Text(if (isResolving) "Getting address..." else "Confirm this location") }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false)
            )
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Selected point",
                tint = Color(0xFF6C5CE7),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 32.dp)
                    .size(48.dp)
            )
            if (isLocatingUser) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Text("Locating you...", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}