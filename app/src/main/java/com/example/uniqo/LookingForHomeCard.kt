package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LookingForHomeCard(
    preferences: List<LookingForPreference>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏠 Currently Looking For", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }

        if (preferences.isEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text("What are you looking for right now?", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        } else {
            Spacer(Modifier.height(10.dp))
            preferences.take(2).forEach { pref ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(PastelLavender),
                        contentAlignment = Alignment.Center
                    ) { Text(pref.category.icon, fontSize = 14.sp) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(pref.category.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(pref.summaryLine(), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            if (preferences.size > 2) {
                Spacer(Modifier.height(4.dp))
                Text("+${preferences.size - 2} more", style = MaterialTheme.typography.bodySmall, color = PurplePrimary)
            }
        }
    }
}