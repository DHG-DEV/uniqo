package com.example.uniqo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class GeoPoint(val latitude: Double, val longitude: Double, val address: String?)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

/**
 * Fetches the device's current GPS location once, then reverse-geocodes it into a
 * human-readable address. Returns null if permission is missing, GPS is off/unavailable,
 * or the request times out — callers should show an appropriate message in that case.
 */
suspend fun getCurrentLocation(context: Context): GeoPoint? {
    if (!hasLocationPermission(context)) return null
    val client = LocationServices.getFusedLocationProviderClient(context)
    val location: Location = suspendCancellableCoroutine { cont ->
        try {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { cont.resume(null) }
        } catch (e: SecurityException) {
            cont.resume(null)
        }
    } ?: return null

    val address = reverseGeocode(context, location.latitude, location.longitude)
    return GeoPoint(location.latitude, location.longitude, address)
}

/** Converts lat/lng into a short readable address like "ABC College Road, Pune". Returns null on failure. */
fun reverseGeocode(context: Context, lat: Double, lng: Double): String? =
    try {
        @Suppress("DEPRECATION")
        val results = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
        results?.firstOrNull()?.let { addr ->
            listOfNotNull(addr.thoroughfare, addr.subLocality ?: addr.locality)
                .joinToString(", ")
                .ifBlank { addr.getAddressLine(0) }
        }
    } catch (e: Exception) {
        null
    }

/** Distance in km between two coordinates, using Android's built-in accurate formula. */
fun distanceKmBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val result = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, result)
    return result[0] / 1000.0
}