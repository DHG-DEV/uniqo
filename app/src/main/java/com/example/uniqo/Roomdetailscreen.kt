package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RoomDetailScreen(
    roomId: String,
    repository: UniqoRepository,
    onBack: () -> Unit,
    onContactOwner: (String) -> Unit
) {
    val rooms by repository.roomListings.collectAsState()
    val room = rooms.firstOrNull { it.id == roomId } ?: return

    Scaffold(
        containerColor = Background,
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
                Button(
                    onClick = { onContactOwner(room.owner.id) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) { Text("Contact / Chat") }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                PlaceholderImage(seed = room.imageRes, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp))
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = CardWhite.copy(alpha = 0.9f))
                ) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(room.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                PriceText(room.rentPerMonth, suffix = " / month", large = true)
                Spacer(Modifier.height(4.dp))
                Text("${room.furnishing.label}${if (room.foodIncluded) " • Food included" else ""}", color = TextSecondary)
                Text("Posted ${relativeTimeFrom(room.createdAt)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Divider)
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(url = room.owner.avatarUrl, size = 44)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(room.owner.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (room.owner.verification.isVerified) VerifiedBadge(compact = true)
                        else Text("Landlord / PG owner", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(room.description, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(20.dp))
                DistanceBadge(room.distanceKm)
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}