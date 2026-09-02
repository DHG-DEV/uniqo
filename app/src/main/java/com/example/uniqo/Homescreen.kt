package com.example.uniqo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: UniqoRepository,
    onSearchClick: () -> Unit,
    onCategoryClick: (ListingCategory?) -> Unit,
    onSeeAllCategories: () -> Unit,
    onListingClick: (String) -> Unit,
    onRoomClick: (String) -> Unit,
    onSeeAllRooms: () -> Unit,
    onNotificationsClick: () -> Unit,
    onPostClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLookingForClick: () -> Unit
) {
    val listings by repository.listings.collectAsState()
    val roomListings by repository.roomListings.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val lookingForPreferences by repository.lookingForPreferences.collectAsState()
    val user = repository.currentUser()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val myPoint by LocationStore.location.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            coroutineScope.launch { LocationStore.ensureLocation(context) }
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            LocationStore.ensureLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // ---------- HEADER ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Hello, ${user.name.substringBefore(" ")} \uD83D\uDC4B",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Welcome back!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // ADD-ON (removal): the "+" post button that used to live here
                // has moved to the bottom bar's floating center button. Nothing
                // else in this header row changed.

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                UserAvatar(
                    url = user.avatarUrl,
                    size = 40,
                    modifier = Modifier.clickable(onClick = onProfileClick)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------- SEARCH ----------
        SearchBar(
            placeholder = "Search rooms, furniture, roommates...",
            onClick = onSearchClick
        )

        Spacer(Modifier.height(28.dp))

        LookingForHomeCard(
            preferences = lookingForPreferences,
            onClick = onLookingForClick
        )

        Spacer(Modifier.height(28.dp))

        // ---------- CATEGORIES ----------
        SectionHeader(
            title = "Categories",
            actionLabel = "See all",
            onAction = onSeeAllCategories
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryChip(
                "Furniture",
                {
                    Icon(
                        Icons.Default.Chair,
                        contentDescription = null,
                        tint = IconBlue,
                        modifier = Modifier.size(22.dp)
                    )
                },
                PastelBlue,
                { onCategoryClick(ListingCategory.FURNITURE) }
            )

            CategoryChip(
                "Rooms",
                {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = IconGreen,
                        modifier = Modifier.size(22.dp)
                    )
                },
                PastelGreen,
                { onCategoryClick(null) }
            )

            CategoryChip(
                "Roommates",
                {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = IconOrange,
                        modifier = Modifier.size(22.dp)
                    )
                },
                PastelPeach,
                { onCategoryClick(null) }
            )

            CategoryChip(
                "Electronics",
                {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = null,
                        tint = IconPurple,
                        modifier = Modifier.size(22.dp)
                    )
                },
                PastelLavender,
                { onCategoryClick(ListingCategory.ELECTRONICS) }
            )

            CategoryChip(
                "Books",
                {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        tint = IconPink,
                        modifier = Modifier.size(22.dp)
                    )
                },
                PastelPink,
                { onCategoryClick(ListingCategory.BOOKS) }
            )
        }

        Spacer(Modifier.height(32.dp))

        // ---------- NEARBY FOR YOU ----------
        SectionHeader(
            title = "Nearby for you",
            actionLabel = "See all",
            onAction = onSeeAllCategories
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listings
                .sortedBy { LocationStore.distanceKmFor(it) ?: Double.MAX_VALUE }
                .take(6)
                .forEach { listing ->
                    ListingCard(
                        listing = listing,
                        isFavorite = favorites.contains(listing.id),
                        onClick = { onListingClick(listing.id) },
                        onFavoriteClick = {
                            repository.toggleFavorite(listing.id)
                        },
                        distanceKm = LocationStore.distanceKmFor(listing)
                    )
                }
        }

        // ---------- POPULAR ROOMS ----------
        if (roomListings.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))

            SectionHeader(
                title = "Popular rooms",
                actionLabel = "See all",
                onAction = onSeeAllRooms
            )

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                roomListings
                    .sortedBy { it.distanceKm }
                    .take(4)
                    .forEach { room ->
                        RoomCard(
                            room = room,
                            onClick = { onRoomClick(room.id) },
                            distanceKm = room.distanceKm
                        )
                    }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}