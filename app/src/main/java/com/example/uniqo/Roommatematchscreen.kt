package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RoommateMatchScreen(
    repository: UniqoRepository,
    onViewProfile: (String) -> Unit,
    onChat: (String) -> Unit
) {

    val preferences by repository.myPreferences.collectAsState()

    var showEdit by remember {
        mutableStateOf(false)
    }

    /*
     * IMPORTANT:
     *
     * Candidates come from the repository.
     * Matching is calculated from the user's CURRENT preferences.
     *
     * This means Profile → My Matches does not use fake
     * hard-coded matches.
     */
    val candidates = remember(repository) {
        repository.roommateCandidates()
    }

    val matches = remember(
        preferences,
        candidates
    ) {
        RoommateMatcher.rank(
            preferences,
            candidates
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                Modifier.height(20.dp)
            )

            Text(
                "Roommate Match",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                Modifier.height(16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CardWhite,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        "Your Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "Edit",
                        color = PurplePrimary,
                        modifier = Modifier.clickable {
                            showEdit = true
                        }
                    )
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                PrefRow(
                    "Budget",
                    "₹${preferences.budgetMin} – ₹${preferences.budgetMax}"
                )

                PrefRow(
                    "Sleep Schedule",
                    preferences.sleepSchedule.label
                )

                PrefRow(
                    "Smoking",
                    preferences.smoking.label
                )

                PrefRow(
                    "Food",
                    preferences.food.label
                )

                PrefRow(
                    "Cleanliness",
                    preferences.cleanliness.label
                )

                PrefRow(
                    "Pets",
                    preferences.pets.label
                )

                PrefRow(
                    "Study Environment",
                    preferences.studyEnvironment.label
                )

                PrefRow(
                    "Maximum Distance",
                    "${preferences.maxDistanceKm} km"
                )
            }

            Spacer(
                Modifier.height(24.dp)
            )

            Text(
                "Best Matches for you",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                Modifier.height(14.dp)
            )
        }

        if (matches.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        "No matching roommates yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Text(
                        "Try widening your budget, distance or preferences.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                matches.forEach { match ->

                    RoommateMatchCard(
                        match = match,

                        onViewProfile = {
                            onViewProfile(
                                match.candidate.student.id
                            )
                        },

                        onChat = {
                            onChat(
                                match.candidate.student.id
                            )
                        }
                    )
                }

                Spacer(
                    Modifier.height(24.dp)
                )
            }
        }
    }

    if (showEdit) {

        EditPreferencesSheet(
            initial = preferences,

            onDismiss = {
                showEdit = false
            },

            onSave = {
                repository.updatePreferences(it)
                showEdit = false
            }
        )
    }
}

@Composable
private fun PrefRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            value,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoommateMatchCard(
    match: RoommateMatch,
    onViewProfile: () -> Unit,
    onChat: () -> Unit
) {

    val candidate = match.candidate

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                CardWhite,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            UserAvatar(
                url = candidate.student.avatarUrl,
                size = 52
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    candidate.student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    "${candidate.student.year}, ${candidate.student.course}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "₹${candidate.preferences.budgetMin} – ₹${candidate.preferences.budgetMax}  •  " +
                            "${"%.1f".format(candidate.distanceKm)} km away",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            MatchBadge(
                percent = match.scorePercent
            )
        }

        Spacer(
            Modifier.height(12.dp)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            OutlinedButton(
                onClick = onViewProfile,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("View Profile")
            }

            Button(
                onClick = onChat,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary
                )
            ) {
                Text("Chat")
            }
        }
    }
}

@Composable
private fun MatchBadge(
    percent: Int
) {

    val color = when {
        percent >= 85 -> SuccessGreen
        percent >= 65 -> WarningAmber
        else -> ErrorRed
    }

    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.12f),
                RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {

        Text(
            "$percent% Match",
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EditPreferencesSheet(
    initial: RoommatePreferences,
    onDismiss: () -> Unit,
    onSave: (RoommatePreferences) -> Unit
) {

    var budgetMin by remember {
        mutableStateOf(
            initial.budgetMin.toString()
        )
    }

    var budgetMax by remember {
        mutableStateOf(
            initial.budgetMax.toString()
        )
    }

    var sleep by remember {
        mutableStateOf(
            initial.sleepSchedule
        )
    }

    var smoking by remember {
        mutableStateOf(
            initial.smoking
        )
    }

    var food by remember {
        mutableStateOf(
            initial.food
        )
    }

    var cleanliness by remember {
        mutableStateOf(
            initial.cleanliness
        )
    }

    var pets by remember {
        mutableStateOf(
            initial.pets
        )
    }

    var study by remember {
        mutableStateOf(
            initial.studyEnvironment
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Edit Preferences")
        },

        text = {

            Column {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value = budgetMin,
                        onValueChange = {
                            budgetMin = it
                        },
                        label = {
                            Text("Min ₹")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = budgetMax,
                        onValueChange = {
                            budgetMax = it
                        },
                        label = {
                            Text("Max ₹")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                EnumSelector(
                    "Sleep",
                    SleepSchedule.values().toList(),
                    sleep
                ) {
                    sleep = it
                }

                EnumSelector(
                    "Smoking",
                    YesNo.values().toList(),
                    smoking
                ) {
                    smoking = it
                }

                EnumSelector(
                    "Food",
                    FoodPref.values().toList(),
                    food
                ) {
                    food = it
                }

                EnumSelector(
                    "Cleanliness",
                    CleanlinessLevel.values().toList(),
                    cleanliness
                ) {
                    cleanliness = it
                }

                EnumSelector(
                    "Pets",
                    YesNo.values().toList(),
                    pets
                ) {
                    pets = it
                }

                EnumSelector(
                    "Study",
                    StudyEnvironment.values().toList(),
                    study
                ) {
                    study = it
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    onSave(
                        initial.copy(
                            budgetMin =
                                budgetMin.toIntOrNull()
                                    ?: initial.budgetMin,

                            budgetMax =
                                budgetMax.toIntOrNull()
                                    ?: initial.budgetMax,

                            sleepSchedule = sleep,
                            smoking = smoking,
                            food = food,
                            cleanliness = cleanliness,
                            pets = pets,
                            studyEnvironment = study
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun <T> EnumSelector(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) where T : Enum<T> {

    Column(
        modifier = Modifier.padding(
            vertical = 6.dp
        )
    ) {

        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(
            Modifier.height(4.dp)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            options.forEach { option ->

                val label = when (option) {

                    is SleepSchedule ->
                        option.label

                    is YesNo ->
                        option.label

                    is FoodPref ->
                        option.label

                    is CleanlinessLevel ->
                        option.label

                    is StudyEnvironment ->
                        option.label

                    else ->
                        option.toString()
                }

                val isSelected =
                    option == selected

                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) {
                                PurplePrimary
                            } else {
                                PurpleLight
                            },
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            onSelect(option)
                        }
                        .padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                ) {

                    Text(
                        label,
                        color =
                            if (isSelected) {
                                Color.White
                            } else {
                                PurpleDeep
                            },
                        style =
                            MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}