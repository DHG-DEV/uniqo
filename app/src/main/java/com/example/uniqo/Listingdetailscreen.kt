package com.example.uniqo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    repository: UniqoRepository,
    onBack: () -> Unit,
    onChatSeller: (String) -> Unit
) {
    val listings by repository.listings.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val listing = listings.firstOrNull { it.id == listingId } ?: return

    val context = LocalContext.current

    // --- Live location: shared across all screens via LocationStore ---
    val myPoint by LocationStore.location.collectAsState()

    LaunchedEffect(listingId) {
        if (hasLocationPermission(context)) {
            LocationStore.ensureLocation(context)
        }
    }

    val liveDistanceKm: Double? = LocationStore.distanceKmFor(listing)

    fun openInMaps() {
        val lat = listing.latitude
        val lng = listing.longitude
        if (lat == null || lng == null) return
        val label = Uri.encode(listing.title)
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onChatSeller(listing.seller.id) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) { Text("Chat") }
                Button(
                    onClick = { repository.sendOffer(listing) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) { Text("Make Offer") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                ListingImage(
                    imageUrl = listing.imageUrl,
                    seed = listing.imageRes,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(0.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack, colors = IconButtonDefaults.iconButtonColors(containerColor = CardWhite.copy(alpha = 0.9f))) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { }, colors = IconButtonDefaults.iconButtonColors(containerColor = CardWhite.copy(alpha = 0.9f))) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        FavoriteButton(isFavorite = favorites.contains(listing.id), onToggle = { repository.toggleFavorite(listing.id) })
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(listing.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                PriceText(listing.price, large = true)
                Spacer(Modifier.height(4.dp))
                Text(listing.condition.label + " condition", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("Posted ${relativeTimeFrom(listing.createdAt)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Divider)
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(url = listing.seller.avatarUrl, size = 44)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(listing.seller.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        VerifiedBadge(compact = true)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(listing.description, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)

                Spacer(Modifier.height(20.dp))
                Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                DetailRow("Category", "${listing.category.label} → ${listing.subCategory}")
                DetailRow("Condition", listing.condition.label)
                DetailRow(
                    "Distance",
                    when {
                        liveDistanceKm == null -> "Location not available"
                        liveDistanceKm < 1.0 -> "${(liveDistanceKm * 1000).toInt()}m away"
                        else -> "${"%.1f".format(liveDistanceKm)} km away"
                    }
                )
                LocationRow(
                    hasCoordinates = listing.latitude != null && listing.longitude != null,
                    onClick = { openInMaps() }
                )

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LocationRow(hasCoordinates: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasCoordinates) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Location", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (hasCoordinates) "View on map" else "Not available",
                color = if (hasCoordinates) PurplePrimary else TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (hasCoordinates) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open in Maps",
                    tint = PurplePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}