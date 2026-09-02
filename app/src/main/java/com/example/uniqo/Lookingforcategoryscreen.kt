package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookingForCategoryScreen(
    onBack: () -> Unit,
    onCategorySelected: (LookingForCategory) -> Unit
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Currently Looking For") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "What are you looking for right now?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(20.dp))

            GroupSection(
                title = "🏠 Housing",
                categories = LookingForCategory.entries.filter { it.group == LookingForGroup.HOUSING },
                onCategorySelected = onCategorySelected
            )

            Spacer(Modifier.height(24.dp))

            GroupSection(
                title = "🛍️ Marketplace",
                categories = LookingForCategory.entries.filter { it.group == LookingForGroup.MARKETPLACE },
                onCategorySelected = onCategorySelected
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GroupSection(
    title: String,
    categories: List<LookingForCategory>,
    onCategorySelected: (LookingForCategory) -> Unit
) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    Spacer(Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { category ->
            CategoryRow(category = category, onClick = { onCategorySelected(category) })
        }
    }
}

@Composable
private fun CategoryRow(category: LookingForCategory, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(PastelLavender),
            contentAlignment = Alignment.Center
        ) {
            Text(category.icon, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(category.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}