package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/* ============================================================================
 * ADD-ON: reverted from the earlier dark-theme version back to the shared
 * light design system (Background/CardWhite/TextPrimary/etc from Color.kt),
 * per "maintain the same visual language on EVERY screen." All the
 * interactive wiring (Chat, Make Offer, tap-to-open) is unchanged.
 * ========================================================================== */

@Composable
private fun notifAvatarColors(name: String): Pair<Color, Color> {
    val palette = listOf(
        PastelBlue to IconBlue,
        PastelGreen to IconGreen,
        PastelPeach to IconOrange,
        PastelLavender to IconPurple,
        PastelPink to IconPink
    )
    return palette[abs(name.hashCode()) % palette.size]
}

private fun notifInitials(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString()
        .ifBlank { "?" }

private fun safeRelativeTime(raw: String): String = runCatching { relativeTimeFrom(raw) }.getOrDefault(raw)

private val notifTabs = listOf("All", "Marketplace", "Rooms", "Roommates")

private fun notifBucket(n: AppNotification): String {
    val cat = n.category?.lowercase().orEmpty()
    return when {
        cat.contains("roommate") -> "Roommates"
        cat.contains("room") -> "Rooms"
        cat.contains("furniture") || cat.contains("electronics") || cat.contains("book") ||
                cat.contains("appliance") || cat.contains("other") -> "Marketplace"
        n.type == NotificationType.OFFER || n.type == NotificationType.MESSAGE ||
                n.type == NotificationType.REPLY || n.type == NotificationType.LIKE -> "Marketplace"
        else -> "Other"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenMyListings: () -> Unit,
    onOpenListing: (String) -> Unit = {},
    onChatSeller: (userId: String, name: String, avatarUrl: String) -> Unit = { _, _, _ -> onOpenChats() },
    onOpenSettings: () -> Unit = {}
) {
    val notifications by repository.notifications.collectAsState()
    val listings by repository.listings.collectAsState()
    val roomListings by repository.roomListings.collectAsState()
    val currentUser by repository.currentUserProfile.collectAsState()

    var activeTab by remember { mutableStateOf("All") }
    var announcementDialog by remember { mutableStateOf<AppNotification?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { repository.markNotificationsRead() }

    val filtered = if (activeTab == "All") notifications else notifications.filter { notifBucket(it) == activeTab }

    if (announcementDialog != null) {
        val notif = announcementDialog!!
        AlertDialog(
            onDismissRequest = { announcementDialog = null },
            title = { Text(notif.actor, fontWeight = FontWeight.Bold) },
            text = { Text(notif.message) },
            confirmButton = { TextButton(onClick = { announcementDialog = null }) { Text("OK", color = PurplePrimary) } }
        )
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(Background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notifTabs) { label ->
                        FilterChip(label = label, selected = activeTab == label, onClick = { activeTab = label })
                    }
                }
            }

            if (filtered.isEmpty()) {
                item { NotifEmptyState() }
            }

            items(filtered, key = { it.id }) { notif ->
                val relatedListing = notif.relatedId?.let { id -> listings.firstOrNull { it.id == id } }
                val relatedRoom = if (relatedListing == null) notif.relatedId?.let { id -> roomListings.firstOrNull { it.id == id } } else null

                val sellerId = relatedListing?.seller?.id ?: relatedRoom?.owner?.id ?: notif.actorId
                val sellerName = relatedListing?.seller?.name ?: relatedRoom?.owner?.name ?: notif.actor
                val sellerAvatar = relatedListing?.seller?.avatarUrl ?: relatedRoom?.owner?.avatarUrl ?: notif.avatarUrl
                val showActions = sellerId != null && sellerId != currentUser.id &&
                        notif.type != NotificationType.ANNOUNCEMENT

                ReferenceNotificationCard(
                    notif = notif,
                    listing = relatedListing,
                    room = relatedRoom,
                    sellerName = sellerName,
                    sellerAvatar = sellerAvatar,
                    showActions = showActions,
                    onCardClick = {
                        if (notif.type == NotificationType.ANNOUNCEMENT) {
                            announcementDialog = notif
                        } else {
                            notif.relatedId?.let(onOpenListing) ?: onOpenMyListings()
                        }
                    },
                    onChatClick = {
                        if (sellerId != null) onChatSeller(sellerId, sellerName, sellerAvatar) else onOpenChats()
                    },
                    onMakeOfferClick = {
                        if (relatedListing != null) {
                            repository.sendOffer(relatedListing)
                            scope.launch { snackbarHostState.showSnackbar("Offer sent to ${relatedListing.seller.name}") }
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ReferenceNotificationCard(
    notif: AppNotification,
    listing: Listing?,
    room: RoomListing?,
    sellerName: String,
    sellerAvatar: String,
    showActions: Boolean,
    onCardClick: () -> Unit,
    onChatClick: () -> Unit,
    onMakeOfferClick: () -> Unit
) {
    val (avatarBg, avatarFg) = notifAvatarColors(sellerName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp), clip = false)
            .clip(RoundedCornerShape(22.dp))
            .background(CardWhite)
            .clickable(onClick = onCardClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (sellerAvatar.isNotBlank()) {
                UserAvatar(url = sellerAvatar, size = 40)
            } else {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(notifInitials(sellerName), color = avatarFg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        sellerName,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    if (notif.isUnread) {
                        Spacer(Modifier.width(6.dp))
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(PurplePrimary))
                    }
                }
                Text(safeRelativeTime(notif.time), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }

            if (notif.type == NotificationType.ANNOUNCEMENT) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
            } else if (notif.type == NotificationType.OFFER) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            notif.message,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2
        )

        val imageUrl = listing?.imageUrl ?: room?.imageUrl
        val title = listing?.title ?: room?.title
        val priceLabel = listing?.let { "₹${it.price}" } ?: room?.let { "₹${it.rentPerMonth}/mo" }
        val locationLabel = listing?.address ?: room?.address
        val statusLabel = listing?.condition?.label ?: room?.type?.label

        if (title != null) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Background)
                    .border(1.dp, Divider, RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                ListingImage(
                    imageUrl = imageUrl,
                    seed = listing?.imageRes ?: room?.imageRes ?: 0,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (priceLabel != null) {
                            Text(priceLabel, color = PurplePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        if (!locationLabel.isNullOrBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(locationLabel, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    if (statusLabel != null) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PurpleLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(statusLabel, color = PurplePrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        if (showActions) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Divider),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Chat", style = MaterialTheme.typography.labelLarge)
                }

                if (listing != null) {
                    Button(
                        onClick = onMakeOfferClick,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White)
                    ) {
                        Text("Make Offer", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(22.dp), clip = false)
            .clip(RoundedCornerShape(22.dp))
            .background(CardWhite)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text("You're all caught up", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("No notifications to show.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}