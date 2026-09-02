package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onListingClick: (String) -> Unit
) {
    val listings by repository.listings.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val me by repository.currentUserProfile.collectAsState()
    val mine = listings.filter { it.seller.id == me.id }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("My Listings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp).fillMaxSize()) {
            Spacer(Modifier.height(4.dp))
            if (mine.isEmpty()) {
                EmptyState("No listings yet", "Post your first item from the Marketplace tab.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mine.chunked(2)) { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { listing ->
                                MarketplaceListingCard(
                                    listing = listing,
                                    isFavorite = favorites.contains(listing.id),
                                    onClick = { onListingClick(listing.id) },
                                    onFavoriteClick = { repository.toggleFavorite(listing.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}