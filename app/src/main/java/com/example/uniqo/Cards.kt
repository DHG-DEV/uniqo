package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ADD-ON: shared corner radius for elevated cards, per the design system spec (20-24dp).
private val CardRadius = 22.dp

/** Horizontal card used on Home ("Nearby for you"). */
@Composable
fun ListingCard(
    listing: Listing,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    distanceKm: Double? = null, // live-computed by the caller; null = not available yet
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(CardRadius), clip = false)
            .clip(RoundedCornerShape(CardRadius))
            .background(CardWhite)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(64.dp)) {
            ListingImage(imageUrl = listing.imageUrl, seed = listing.imageRes, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(listing.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            PriceText(listing.price)
            Spacer(Modifier.height(4.dp))
            DistanceBadge(distanceKm)
        }
        FavoriteButton(isFavorite = isFavorite, onToggle = onFavoriteClick)
    }
}

/** Vertical grid-style card used on the Marketplace screen. */
@Composable
fun MarketplaceListingCard(
    listing: Listing,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    distanceKm: Double? = null,
    distanceLabelOverride: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(CardRadius), clip = false)
            .clip(RoundedCornerShape(CardRadius))
            .background(CardWhite)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            ListingImage(
                imageUrl = listing.imageUrl,
                seed = listing.imageRes,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = CardRadius, topEnd = CardRadius)
            )
            FavoriteButton(
                isFavorite = isFavorite,
                onToggle = onFavoriteClick,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            )
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(listing.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            PriceText(listing.price)
            Spacer(Modifier.height(4.dp))
            Text(listing.condition.label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(2.dp))

            val distanceText = when {
                distanceLabelOverride != null -> distanceLabelOverride
                distanceKm == null -> "Location not available"
                distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()}m away"
                else -> "${"%.1f".format(distanceKm)} km away"
            }
            Text(distanceText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
fun RoomCard(
    room: RoomListing,
    onClick: () -> Unit,
    distanceKm: Double? = null, // live-computed by the caller; null = not available yet
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(CardRadius), clip = false)
            .clip(RoundedCornerShape(CardRadius))
            .background(CardWhite)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(72.dp)) {
            ListingImage(imageUrl = room.imageUrl, seed = room.imageRes, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(room.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            PriceText(room.rentPerMonth, suffix = " / month")
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (room.foodIncluded) "Food included" else room.furnishing.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (room.foodIncluded) {
                    Spacer(Modifier.width(4.dp))
                }
            }
            Spacer(Modifier.height(2.dp))
            DistanceBadge(distanceKm)
        }
    }
}