package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun MarketplaceScreen(
    repository: UniqoRepository,
    initialCategory: ListingCategory? = null,
    onListingClick: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    val allListings by repository.listings.collectAsState()
    val favorites by repository.favorites.collectAsState()
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var query by remember { mutableStateOf("") }

    val filtered = allListings.filter { listing ->
        (selectedCategory == null || listing.category == selectedCategory) &&
                (query.isBlank() || listing.title.contains(query, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Marketplace", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = TextPrimary)
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
                    // ADD-ON: shared theme constants + subtle shadow instead of a
                    // hardcoded Color.White, matching SearchBar's look elsewhere.
                    .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), clip = false)
                    .background(CardWhite, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
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
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
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
                gridItems(filtered) { listing ->
                    MarketplaceListingCard(
                        listing = listing,
                        isFavorite = favorites.contains(listing.id),
                        onClick = { onListingClick(listing.id) },
                        onFavoriteClick = { repository.toggleFavorite(listing.id) }
                    )
                }
            }
        }
    }
}