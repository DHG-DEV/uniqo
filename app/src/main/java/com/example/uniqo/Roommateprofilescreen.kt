@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RoommateProfileScreen(
    candidateId: String,
    repository: UniqoRepository,
    onBack: () -> Unit,
    onChat: (String) -> Unit
) {
    val preferences by repository.myPreferences.collectAsState()
    val candidate = remember { repository.roommateCandidates().firstOrNull { it.student.id == candidateId } } ?: return
    val match = remember(preferences) { RoommateMatcher.score(preferences, candidate) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(candidate.student.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
                Button(
                    onClick = { onChat(candidate.student.id) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) { Text("Chat") }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(url = candidate.student.avatarUrl, size = 64)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(candidate.student.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${candidate.student.year}, ${candidate.student.course}", color = TextSecondary)
                    VerifiedBadge(compact = true)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Compatibility: ${match.scorePercent}%", style = MaterialTheme.typography.titleLarge, color = PurplePrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            match.breakdown.forEach { (label, score) ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text("$score pts", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = (score / 20f).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = PurplePrimary,
                        trackColor = Divider
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            listOf(
                "Budget" to "₹${candidate.preferences.budgetMin} – ₹${candidate.preferences.budgetMax}",
                "Sleep Schedule" to candidate.preferences.sleepSchedule.label,
                "Smoking" to candidate.preferences.smoking.label,
                "Food" to candidate.preferences.food.label,
                "Cleanliness" to candidate.preferences.cleanliness.label,
                "Pets" to candidate.preferences.pets.label,
                "Study Environment" to candidate.preferences.studyEnvironment.label,
            ).forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = TextSecondary)
                    Text(value, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}