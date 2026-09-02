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
fun BookmarksScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onListingClick: (String) -> Unit
) {
    val listings by repository.listings.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val favoriteListings = listings.filter { favorites.contains(it.id) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("My Bookmarks") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp).fillMaxSize()) {
            Spacer(Modifier.height(4.dp))
            if (favoriteListings.isEmpty()) {
                EmptyState("No bookmarks yet", "Tap the heart icon on any listing, room, or roommate to save it here.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(favoriteListings) { listing ->
                        ListingCard(
                            listing = listing,
                            isFavorite = true,
                            onClick = { onListingClick(listing.id) },
                            onFavoriteClick = { repository.toggleFavorite(listing.id) }
                        )
                    }
                }
            }
        }
    }
}