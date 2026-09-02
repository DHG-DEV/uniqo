package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MarketplaceScreen(
    repository: UniqoRepository,
    onListingClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    onPostClick: () -> Unit
) {
    val context = LocalContext.current
    val allListings by repository.listings.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val activeFilters by repository.activeFilters.collectAsState()
    var query by remember { mutableStateOf("") }

    // Same call, same source, same result as HomeScreen — nothing screen-specific here.
    val myPoint by LocationStore.location.collectAsState()

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            LocationStore.ensureLocation(context)
        }
    }

    val filtered = allListings.filter { listing ->
        val dist = LocationStore.distanceKmFor(listing)
        (activeFilters.category == null || listing.category == activeFilters.category) &&
                (activeFilters.condition == null || listing.condition == activeFilters.condition) &&
                listing.price >= activeFilters.minPrice &&
                listing.price <= activeFilters.maxPrice &&
                (dist == null || dist <= activeFilters.maxDistanceKm) &&
                (query.isBlank() || listing.title.contains(query, ignoreCase = true))
    }.let { list ->
        when (activeFilters.sortBy) {
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOption.NEAREST -> list.sortedBy { LocationStore.distanceKmFor(it) ?: Double.MAX_VALUE }
            SortOption.NEWEST -> list.sortedBy { it.postedDaysAgo }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Marketplace", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFilterClick) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters")
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = onPostClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = PurplePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Post a listing", tint = Color.White)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        androidx.compose.foundation.text.BasicTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth()
        ) { innerTextField ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text("Search furniture, appliances...", color = TextSecondary)
                }
                innerTextField()
            }
        }

        Spacer(Modifier.height(14.dp))

        val categories = listOf<ListingCategory?>(null) + ListingCategory.values().toList()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                FilterChip(
                    label = cat?.label ?: "All",
                    selected = activeFilters.category == cat,
                    onClick = { repository.setCategoryFilter(cat) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            EmptyState("No listings found", "Try a different search or category.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                gridItems(filtered, key = { it.id }) { listing ->
                    MarketplaceListingCard(
                        listing = listing,
                        isFavorite = favorites.contains(listing.id),
                        onClick = { onListingClick(listing.id) },
                        onFavoriteClick = { repository.toggleFavorite(listing.id) },
                        distanceKm = LocationStore.distanceKmFor(listing)
                    )
                }
            }
        }
    }
}