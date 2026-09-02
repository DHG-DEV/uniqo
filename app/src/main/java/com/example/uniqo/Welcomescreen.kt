package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

private data class Feature(val icon: ImageVector, val label: String, val tint: Color)

private val features = listOf(
    Feature(Icons.Default.Chair, "Buy & Sell Furniture", IconBlue),
    Feature(Icons.Default.MeetingRoom, "Find Rooms & PGs", IconGreen),
    Feature(Icons.Default.People, "Find Roommates", IconOrange),
    Feature(Icons.Default.LocationOn, "Nearby Deals", IconPurple),
    Feature(Icons.Default.Chat, "Chat & Connect", IconPink),
    Feature(Icons.Default.Shield, "Trusted Student Community", IconGreen),
)

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(56.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PurplePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Spacer(Modifier.height(20.dp))

        Text(
            buildAnnotatedBrand(),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        Text("Your Campus.", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Your Community.", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Text(
            "Everything you need, right around you.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(32.dp))

        Text("Key Features", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(feature.icon, contentDescription = null, tint = feature.tint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(feature.label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun buildAnnotatedBrand() = androidx.compose.ui.text.buildAnnotatedString {
    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
        append("Uni")
    }
    withStyle(style = androidx.compose.ui.text.SpanStyle(color = PurplePrimary, fontWeight = FontWeight.Bold)) {
        append("qo")
    }
}