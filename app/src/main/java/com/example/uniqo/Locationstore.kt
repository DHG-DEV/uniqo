package com.example.uniqo

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A central store for the user's current GPS location.
 * Shared across Home, Marketplace, and Listing details to show real-time distances.
 */
object LocationStore {
    private val _location = MutableStateFlow<GeoPoint?>(null)
    val location: StateFlow<GeoPoint?> = _location.asStateFlow()

    suspend fun ensureLocation(context: Context) {
        if (_location.value == null) {
            _location.value = getCurrentLocation(context)
        }
    }

    /** Helper to calculate distance from current location to a listing/room. */
    fun distanceKmFor(listing: Listing): Double? {
        val current = _location.value ?: return null
        val lat = listing.latitude ?: return null
        val lng = listing.longitude ?: return null
        return distanceKmBetween(current.latitude, current.longitude, lat, lng)
    }

    fun distanceKmFor(room: RoomListing): Double? {
        val current = _location.value ?: return null
        val lat = room.latitude ?: return null
        val lng = room.longitude ?: return null
        return distanceKmBetween(current.latitude, current.longitude, lat, lng)
    }
}
