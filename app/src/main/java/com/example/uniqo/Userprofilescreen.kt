package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private fun UniqoRepository.findStudent(id: String): Student? {
    if (currentUser().id == id) return currentUser()
    conversations.value.firstOrNull { it.participant.id == id }?.let { return it.participant }
    listings.value.firstOrNull { it.seller.id == id }?.let { return it.seller }
    roomListings.value.firstOrNull { it.owner.id == id }?.let { return it.owner }
    roommateCandidates().firstOrNull { it.student.id == id }?.let { return it.student }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    repository: UniqoRepository,
    onBack: () -> Unit,
    onChat: (String) -> Unit,
    onOpenListing: (String) -> Unit,
    onOpenRoom: (String) -> Unit
) {
    val listings by repository.listings.collectAsState()
    val rooms by repository.roomListings.collectAsState()
    val conversations by repository.conversations.collectAsState()
    val student = remember(userId, listings, rooms, conversations) {
        repository.findStudent(userId)
    } ?: Student(userId, "User", "", "", "", "")

    val theirListings = listings.filter { it.seller.id == userId }
    val theirRooms = rooms.filter { it.owner.id == userId }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(student.name) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Button(
                onClick = { onChat(student.id) },
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) { Text("Message") }
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(student.avatarUrl, 72)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(student.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        PulsePresence(student.lastActiveAt)
                        if (student.college.isNotBlank()) {
                            val info = listOf(student.course, student.year).filter(String::isNotBlank).joinToString(", ")
                            if (info.isNotBlank()) Text(info, color = TextSecondary)
                            Text(student.college, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        if (student.verification.isVerified) VerifiedBadge(compact = true)
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardWhite).padding(vertical = 14.dp),
                    Arrangement.SpaceEvenly
                ) {
                    ProfileStat("Rating") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = IconOrange)
                            Spacer(Modifier.width(2.dp))
                            Text("%.1f".format(student.rating), fontWeight = FontWeight.Bold)
                        }
                    }
                    ProfileStat("Listings") { Text("${student.listingsCount}", fontWeight = FontWeight.Bold) }
                    ProfileStat("Sold") { Text("${student.soldCount}", fontWeight = FontWeight.Bold) }
                }

                if (theirListings.isNotEmpty() || theirRooms.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text("Active listings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                }
            }
            items(theirListings) { listing ->
                SimpleListingRow(listing.title, "₹${listing.price}") { onOpenListing(listing.id) }
            }
            items(theirRooms) { room ->
                SimpleListingRow(room.title, "₹${room.rentPerMonth}/mo") { onOpenRoom(room.id) }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        value()
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun SimpleListingRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardWhite).clickable(onClick = onClick).padding(14.dp),
        Arrangement.SpaceBetween
    ) {
        Text(title, fontWeight = FontWeight.Medium)
        Text(subtitle, color = TextSecondary)
    }
}