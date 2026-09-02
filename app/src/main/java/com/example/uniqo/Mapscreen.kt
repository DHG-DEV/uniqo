package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Map discovery screen. This renders a lightweight custom-drawn "map" fallback
 * (positions listings on a grid with pins) so the screen is fully interactive
 * without a Maps API key. To go live: drop this Box for a
 * com.google.android.gms.maps.compose GoogleMap composable, keep MapPin below
 * for marker data, and reuse the same bottom preview card.
 */
@Composable
fun MapScreen(
    repository: UniqoRepository,
    onListingClick: (String) -> Unit
) {
    val listings by repository.listings.collectAsState()
    var categoryFilter by remember { mutableStateOf("All") }

    // This map only plots marketplace listings (Furniture/Electronics/Books/etc.) —
    // Rooms and Roommates live in separate data sources not yet plotted here.
    val visibleListings = when (categoryFilter) {
        "Furniture" -> listings.filter { it.category == ListingCategory.FURNITURE }
        "Rooms", "Roommates" -> emptyList()
        else -> listings
    }.take(6)

    var selected by remember { mutableStateOf(visibleListings.firstOrNull()) }
    LaunchedEffect(categoryFilter) { selected = visibleListings.firstOrNull() }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            SearchBar(placeholder = "Search this area")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Furniture", "Rooms", "Roommates")) { label ->
                    FilterChip(label = label, selected = categoryFilter == label, onClick = { categoryFilter = label })
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PastelGreen.copy(alpha = 0.4f))
        ) {
            // Simple deterministic pin layout standing in for real map coordinates.
            visibleListings.forEachIndexed { index, listing ->
                val xFrac = 0.15f + (index % 3) * 0.32f
                val yFrac = 0.18f + (index / 3) * 0.35f + (sin(index.toDouble()) * 0.05f).toFloat()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (xFrac * 300).dp,
                                y = (yFrac * 420).dp
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected?.id == listing.id) PurplePrimary else CardWhite)
                            .clickable { selected = listing }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "₹${listing.price}${if (listing.category.label == "Rooms") "/mo" else ""}",
                            color = if (selected?.id == listing.id) Color.White else TextPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (visibleListings.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No $categoryFilter pins on this map yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        selected?.let { listing ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardWhite)
                    .clickable { onListingClick(listing.id) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(56.dp)) {
                    PlaceholderImage(seed = listing.imageRes, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    PriceText(listing.price)
                    DistanceBadge(listing.distanceKm)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}