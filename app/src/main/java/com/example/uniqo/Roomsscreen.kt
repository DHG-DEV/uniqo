package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoomsScreen(
    repository: UniqoRepository,
    onRoomClick: (String) -> Unit
) {
    val rooms by repository.roomListings.collectAsState()
    var selectedType by remember { mutableStateOf<RoomType?>(null) }
    var query by remember { mutableStateOf("") }

    val filtered = rooms.filter { room ->
        (selectedType == null || room.type == selectedType) &&
                (query.isBlank() || room.title.contains(query, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("Rooms & PGs", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(Modifier.height(14.dp))
        SearchBar(placeholder = "Search for rooms, PGs, flats...")

        Spacer(Modifier.height(14.dp))
        val types = listOf<RoomType?>(null) + RoomType.values().toList()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(types) { type ->
                FilterChip(
                    label = type?.label ?: "All",
                    selected = selectedType == type,
                    onClick = { selectedType = type }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            EmptyState("No rooms found", "Try a different filter.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { room ->
                    RoomCard(room = room, onClick = { onRoomClick(room.id) })
                }
            }
        }
    }
}