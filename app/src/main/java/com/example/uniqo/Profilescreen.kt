package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/* ============================================================================
 * PROFILE SCREEN — restyled to match the reference (full-bleed purple/indigo
 * gradient header, overlapping stats card, floating gradient Edit Profile
 * pill). All data sources, callbacks, sections, and the Lifestyle editor are
 * unchanged from the previous version — this is a visual pass only.
 * ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onMenuClick: (String) -> Unit
) {
    val user by repository.currentUserProfile.collectAsState()
    val allListings by repository.listings.collectAsState()
    val allRoomListings by repository.roomListings.collectAsState()
    val myPreferences by repository.myPreferences.collectAsState()
    val lookingForPrefs by repository.lookingForPreferences.collectAsState()

    val myListings = remember(allListings, user.id) { allListings.filter { it.seller.id == user.id } }
    val myRoomListings = remember(allRoomListings, user.id) { allRoomListings.filter { it.owner.id == user.id } }
    val totalListedCount = myListings.size + myRoomListings.size

    var showLifestyleEditor by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        // ADD-ON: without this, Scaffold reserves top status-bar space by
        // default even with no TopAppBar, leaving a blank gap above the
        // gradient. Zeroing it out lets the header draw truly edge-to-edge.
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        // ADD-ON: no TopAppBar — back/settings now live inside the gradient
        // header itself, matching the reference. onEditProfile is unaffected;
        // it's still triggered by the floating pill button below.
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Background)) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    GradientPillButton(
                        text = "Edit Profile",
                        icon = Icons.Default.Edit,
                        onClick = onEditProfile
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProfileGradientHeader(
                user = user,
                onBack = onBack,
                onSettings = { onMenuClick(Routes.SETTINGS) }
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Pulled up to overlap the header's bottom edge.
                ProfileStatsBar(
                    user = user,
                    listedCount = totalListedCount,
                    modifier = Modifier.offset(y = (-32).dp)
                )

                Spacer(Modifier.height(4.dp))

                ListedArticlesSection(
                    listings = myListings,
                    rooms = myRoomListings,
                    onListingClick = { onMenuClick(Routes.listingDetail(it)) },
                    onRoomClick = { onMenuClick(Routes.roomDetail(it)) },
                    onViewAll = { onMenuClick(Routes.MY_LISTINGS) }
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoommatePreferencesCard(
                        preferences = myPreferences,
                        modifier = Modifier.weight(1f),
                        onViewAll = { onMenuClick(Routes.ROOMMATE_PREFERENCES) }
                    )
                    CurrentlyLookingForCard(
                        preferences = lookingForPrefs,
                        modifier = Modifier.weight(1f),
                        onClick = { onMenuClick(Routes.LOOKING_FOR_CATEGORY_SELECT) }
                    )
                }

                Spacer(Modifier.height(20.dp))

                LifestyleHabitsCard(
                    preferences = myPreferences,
                    onEditClick = { showLifestyleEditor = true }
                )

                Spacer(Modifier.height(20.dp))

                ReviewsRow(rating = user.rating, onClick = { onMenuClick(Routes.REVIEWS_RECEIVED) })

                Spacer(Modifier.height(20.dp))

                // ADD-ON: shown unconditionally now (not just when a location
                // is already set) so there's a way to add one, not just view it.
                LocationCard(location = user.location, onClick = { onMenuClick(Routes.MAP) })
                Spacer(Modifier.height(20.dp))

                VerifiedProfileStrip(verification = user.verification)

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showLifestyleEditor) {
        LifestyleEditSheet(
            preferences = myPreferences,
            onDismiss = { showLifestyleEditor = false },
            onSave = { updated ->
                repository.updatePreferences(updated)
                showLifestyleEditor = false
            }
        )
    }
}

/* -------------------------------------------------------------------------- */
/* GRADIENT HEADER                                                            */
/* -------------------------------------------------------------------------- */

@Composable
private fun ProfileGradientHeader(user: Student, onBack: () -> Unit, onSettings: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Brush.verticalGradient(listOf(PurplePrimary, PurpleDeep)))
    ) {
        // Subtle abstract curved shapes
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-30).dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-70).dp, y = 30.dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    url = user.avatarUrl,
                    size = 96,
                    modifier = Modifier.border(4.dp, Color.White, CircleShape)
                )

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (user.username.isNotBlank()) "@${user.username.removePrefix("@")}" else user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (user.verification.isVerified) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))

                val subline = if (user.bio.isNotBlank()) user.bio else buildAcademicLine(user)
                if (subline.isNotBlank()) {
                    Text(
                        subline,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun buildAcademicLine(user: Student): String {
    val values = listOf(user.year, user.course, user.college).filter { it.isNotBlank() }
    return if (values.isEmpty()) "" else values.joinToString(" • ")
}

/* -------------------------------------------------------------------------- */
/* FLOATING GRADIENT PILL BUTTON                                              */
/* -------------------------------------------------------------------------- */

@Composable
private fun GradientPillButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), clip = false)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(PurplePrimary, PurpleDeep)))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
    }
}

/* -------------------------------------------------------------------------- */
/* STATS BAR (Rating | Transactions | Listed Articles | Verified)            */
/* -------------------------------------------------------------------------- */

@Composable
private fun ProfileStatsBar(user: Student, listedCount: Int, modifier: Modifier = Modifier) {
    ProfileCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem(
                icon = Icons.Default.Star,
                iconTint = StarGold,
                value = String.format("%.1f", user.rating),
                label = "Rating",
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.SwapHoriz,
                iconTint = PurplePrimary,
                value = user.soldCount.toString(),
                label = "Transactions",
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.Article,
                iconTint = PurplePrimary,
                value = listedCount.toString(),
                label = "Listed",
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.Shield,
                iconTint = if (user.verification.isVerified) SuccessGreen else TextSecondary,
                value = if (user.verification.isVerified) "Verified" else "Unverified",
                label = verificationSubLabel(user.verification),
                modifier = Modifier.weight(1.3f)
            )
        }
    }
}

private fun verificationSubLabel(v: VerificationState): String {
    val parts = mutableListOf<String>()
    if (v.phoneVerified) parts.add("Phone")
    if (v.studentVerified) parts.add("College")
    if (v.emailVerified) parts.add("Email")
    return if (parts.isEmpty()) "Not verified" else parts.joinToString(" & ")
}

@Composable
private fun StatItem(icon: ImageVector, iconTint: Color, value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

/* -------------------------------------------------------------------------- */
/* LISTED ARTICLES                                                            */
/* -------------------------------------------------------------------------- */

@Composable
private fun ListedArticlesSection(
    listings: List<Listing>,
    rooms: List<RoomListing>,
    onListingClick: (String) -> Unit,
    onRoomClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    val total = listings.size + rooms.size

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Article, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Listed Articles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            if (total > 0) {
                TextButton(onClick = onViewAll) {
                    Text("View All ($total)", color = PurplePrimary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (total == 0) {
            ProfileCard {
                Text("You haven't listed anything yet.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listings.forEach { listing ->
                    ListedArticleCard(
                        imageUrl = listing.imageUrl,
                        seed = listing.imageRes,
                        title = listing.title,
                        priceLabel = "₹${listing.price}",
                        location = listing.address ?: listing.subCategory,
                        onClick = { onListingClick(listing.id) }
                    )
                }
                rooms.forEach { room ->
                    ListedArticleCard(
                        imageUrl = room.imageUrl,
                        seed = room.imageRes,
                        title = room.title,
                        priceLabel = "₹${room.rentPerMonth}/month",
                        location = room.address ?: room.type.label,
                        onClick = { onRoomClick(room.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ListedArticleCard(
    imageUrl: String?,
    seed: Int,
    title: String,
    priceLabel: String,
    location: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(170.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(CardWhite)
            .clickable { onClick() }
    ) {
        Box {
            ListingImage(
                imageUrl = imageUrl,
                seed = seed,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(18.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PurplePrimary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Available", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, color = TextPrimary)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(2.dp))
                Text(location, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Spacer(Modifier.height(3.dp))
            Text(priceLabel, color = PurplePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ROOMMATE PREFERENCES                                                       */
/* -------------------------------------------------------------------------- */

@Composable
private fun RoommatePreferencesCard(
    preferences: RoommatePreferences,
    modifier: Modifier = Modifier,
    onViewAll: () -> Unit
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.People, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Roommate Prefs", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, color = TextPrimary)
        }

        Spacer(Modifier.height(8.dp))

        ProfileCard(modifier = Modifier.clickable { onViewAll() }) {
            CompactPrefLine(Icons.Default.AttachMoney, "₹${preferences.budgetMin}-${preferences.budgetMax}")
            CompactPrefLine(Icons.Default.Bedtime, preferences.sleepSchedule.label)
            CompactPrefLine(Icons.Default.SmokingRooms, "Smoking: ${preferences.smoking.label}")
            CompactPrefLine(Icons.Default.Restaurant, preferences.food.label)
            CompactPrefLine(Icons.Default.Pets, "Pets: ${preferences.pets.label}", showDivider = false)
        }
    }
}

@Composable
private fun CurrentlyLookingForCard(
    preferences: List<LookingForPreference>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Home, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Looking For", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, color = TextPrimary)
        }

        Spacer(Modifier.height(8.dp))

        ProfileCard(modifier = Modifier.clickable { onClick() }) {
            if (preferences.isEmpty()) {
                Text("Nothing set", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            } else {
                preferences.forEachIndexed { index, pref ->
                    CompactPrefLine(
                        icon = Icons.Default.Circle,
                        text = pref.category.label,
                        showDivider = index != preferences.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactPrefLine(icon: ImageVector, text: String, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1)
        }
        if (showDivider) HorizontalDivider(color = Divider)
    }
}

/* -------------------------------------------------------------------------- */
/* LIFESTYLE & HABITS — editable                                             */
/* -------------------------------------------------------------------------- */

@Composable
private fun LifestyleHabitsCard(preferences: RoommatePreferences, onEditClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiObjects, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Lifestyle & Habits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Lifestyle & Habits", tint = PurplePrimary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        ProfileCard {
            LifestyleRow(Icons.Default.WbSunny, "Sleep Schedule", preferences.sleepSchedule.label)
            LifestyleRow(Icons.Default.SmokingRooms, "Smoking", preferences.smoking.label)
            LifestyleRow(Icons.Default.Restaurant, "Food Preference", preferences.food.label)
            LifestyleRow(Icons.Default.CleaningServices, "Cleanliness", preferences.cleanliness.label)
            LifestyleRow(Icons.Default.Pets, "Pets", preferences.pets.label)
            LifestyleRow(Icons.Default.MenuBook, "Study Environment", preferences.studyEnvironment.label, showDivider = false)
        }
    }
}

@Composable
private fun LifestyleRow(icon: ImageVector, label: String, value: String, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(10.dp))
                Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
        if (showDivider) HorizontalDivider(color = Divider)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LifestyleEditSheet(
    preferences: RoommatePreferences,
    onDismiss: () -> Unit,
    onSave: (RoommatePreferences) -> Unit
) {
    var sleepSchedule by remember { mutableStateOf(preferences.sleepSchedule) }
    var smoking by remember { mutableStateOf(preferences.smoking) }
    var food by remember { mutableStateOf(preferences.food) }
    var cleanliness by remember { mutableStateOf(preferences.cleanliness) }
    var pets by remember { mutableStateOf(preferences.pets) }
    var studyEnvironment by remember { mutableStateOf(preferences.studyEnvironment) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Edit Lifestyle & Habits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            LifestylePickerRow("Sleep Schedule", SleepSchedule.values().toList(), sleepSchedule, { it.label }) { sleepSchedule = it }
            LifestylePickerRow("Smoking", YesNo.values().toList(), smoking, { it.label }) { smoking = it }
            LifestylePickerRow("Food Preference", FoodPref.values().toList(), food, { it.label }) { food = it }
            LifestylePickerRow("Cleanliness", CleanlinessLevel.values().toList(), cleanliness, { it.label }) { cleanliness = it }
            LifestylePickerRow("Pets", YesNo.values().toList(), pets, { it.label }) { pets = it }
            LifestylePickerRow("Study Environment", StudyEnvironment.values().toList(), studyEnvironment, { it.label }) { studyEnvironment = it }

            Spacer(Modifier.height(10.dp))

            GradientPillButton(
                text = "Save Changes",
                icon = Icons.Default.Check,
                onClick = {
                    onSave(
                        preferences.copy(
                            sleepSchedule = sleepSchedule,
                            smoking = smoking,
                            food = food,
                            cleanliness = cleanliness,
                            pets = pets,
                            studyEnvironment = studyEnvironment
                        )
                    )
                }
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun <T> LifestylePickerRow(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(label = labelOf(option), selected = option == selected, onClick = { onSelect(option) })
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* REVIEWS ROW                                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun ReviewsRow(rating: Double, onClick: () -> Unit) {
    ProfileCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Reviews Received", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${String.format("%.1f", rating)} average rating", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* LOCATION                                                                    */
/* -------------------------------------------------------------------------- */

@Composable
private fun LocationCard(location: String, onClick: () -> Unit) {
    val hasLocation = location.isNotBlank()

    ProfileCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (hasLocation) Icons.Default.LocationOn else Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Location", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        if (hasLocation) location else "Add your location",
                        color = if (hasLocation) TextSecondary else PurplePrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (hasLocation) FontWeight.Normal else FontWeight.SemiBold
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* VERIFIED PROFILE STRIP                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun VerifiedProfileStrip(verification: VerificationState) {
    ProfileCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Verified Profile", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            VerificationCheckItem("Phone Verified", verification.phoneVerified)
            VerificationCheckItem("College Verified", verification.studentVerified)
            VerificationCheckItem("Email Verified", verification.emailVerified)
        }
    }
}

@Composable
private fun VerificationCheckItem(label: String, verified: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (verified) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (verified) SuccessGreen else TextSecondary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (verified) TextPrimary else TextSecondary)
    }
}

/* -------------------------------------------------------------------------- */
/* COMMON                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun ProfileCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp), clip = false)
            .clip(RoundedCornerShape(22.dp))
            .background(CardWhite)
            .padding(14.dp),
        content = content
    )
}