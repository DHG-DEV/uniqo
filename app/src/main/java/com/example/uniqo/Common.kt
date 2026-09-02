package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SearchBar(
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite)
            .border(1.dp, Divider, RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(placeholder, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CategoryChip(
    label: String,
    icon: @Composable () -> Unit,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PurplePrimary else CardWhite)
            .border(1.dp, if (selected) PurplePrimary else Divider, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else TextPrimary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Verified",
            tint = SuccessGreen,
            modifier = Modifier.size(if (compact) 12.dp else 15.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            "Verified Student",
            color = SuccessGreen,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriceText(amount: Int, modifier: Modifier = Modifier, suffix: String = "", large: Boolean = false) {
    Text(
        "₹${"%,d".format(amount)}$suffix",
        color = PurplePrimary,
        fontWeight = FontWeight.Bold,
        style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

/** Shows a live distance if known, or a clear fallback when the listing/user location isn't available. */
@Composable
fun DistanceBadge(
    distanceKm: Double?,
    modifier: Modifier = Modifier,
    labelOverride: String? = null
) {
    val label = labelOverride ?: when {
        distanceKm == null -> "Location not available"
        distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()}m away"
        else -> "${"%.1f".format(distanceKm)} km away"
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(2.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Favorite else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun UserAvatar(url: String, size: Int = 44, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PurpleLight)
    )
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PurplePrimary)
    }
}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        if (actionLabel != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = PurplePrimary,
                modifier = Modifier.clickable { onAction?.invoke() }
            )
        }
    }
}

/** Deterministic pastel placeholder image (since we don't ship real product photos). */
@Composable
fun PlaceholderImage(seed: Int, modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp)) {
    val palette = listOf(PastelBlue, PastelGreen, PastelPeach, PastelLavender, PastelPink)
    val color = palette[seed % palette.size]
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
    )
}