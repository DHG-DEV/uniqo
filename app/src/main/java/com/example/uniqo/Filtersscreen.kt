package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    initial: ListingFilters = ListingFilters(),
    onBack: () -> Unit,
    onApply: (ListingFilters) -> Unit
) {
    var category by remember { mutableStateOf(initial.category) }
    var priceRange by remember { mutableStateOf(initial.minPrice.toFloat()..initial.maxPrice.toFloat()) }
    var distance by remember { mutableStateOf(initial.maxDistanceKm.toFloat()) }
    var condition by remember { mutableStateOf(initial.condition) }
    var sort by remember { mutableStateOf(initial.sortBy) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Filters") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TextButton(onClick = {
                        category = null; priceRange = 0f..20000f; distance = 10f; condition = null; sort = SortOption.NEWEST
                    }) { Text("Clear", color = PurplePrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
                Button(
                    onClick = {
                        onApply(
                            ListingFilters(
                                category = category,
                                minPrice = priceRange.start.toInt(),
                                maxPrice = priceRange.endInclusive.toInt(),
                                maxDistanceKm = distance.toDouble(),
                                condition = condition,
                                sortBy = sort
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) { Text("Apply Filters") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            SectionTitle("Category")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cats = listOf<ListingCategory?>(null) + ListingCategory.values().toList()
                cats.forEach { cat ->
                    FilterChip(label = cat?.label ?: "All", selected = category == cat, onClick = { category = cat })
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Price Range")
            RangeSlider(
                value = priceRange,
                onValueChange = { priceRange = it },
                valueRange = 0f..20000f,
                steps = 19
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("₹${priceRange.start.toInt()}", color = TextSecondary)
                Text("₹${priceRange.endInclusive.toInt()}${if (priceRange.endInclusive >= 20000f) "+" else ""}", color = TextSecondary)
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Distance")
            Slider(value = distance, onValueChange = { distance = it }, valueRange = 1f..10f, steps = 8)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Any Distance", color = TextSecondary)
                Text("${distance.toInt()} km", color = TextSecondary)
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Condition")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val conds = listOf<ListingCondition?>(null) + ListingCondition.values().toList()
                conds.forEach { cond ->
                    FilterChip(label = cond?.label ?: "All", selected = condition == cond, onClick = { condition = cond })
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Sort By")
            Column {
                SortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sort == option, onClick = { sort = option }, colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary))
                        Text(option.label)
                    }
                }
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    Spacer(Modifier.height(8.dp))
}