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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val notificationFilterTabs = listOf("All", "Marketplace", "Rooms", "Roommate", "Other")

private fun notificationBucket(n: AppNotification): String {
    val cat = n.category?.lowercase().orEmpty()
    return when {
        n.type == NotificationType.ANNOUNCEMENT -> "Other"
        cat.contains("roommate") -> "Roommate"
        cat.contains("room") -> "Rooms"
        cat.contains("furniture") || cat.contains("electronics") || cat.contains("book") -> "Marketplace"
        n.type == NotificationType.OFFER || n.type == NotificationType.MESSAGE || n.type == NotificationType.REPLY || n.type == NotificationType.LIKE -> "Marketplace"
        else -> "Other"
    }
}

private enum class NotificationSection { OFFERS, POSTS, SYSTEM }

private fun sectionOf(n: AppNotification): NotificationSection = when {
    n.id.startsWith("selfpost_") || n.type == NotificationType.ANNOUNCEMENT -> NotificationSection.SYSTEM
    n.type == NotificationType.OFFER || n.type == NotificationType.MESSAGE || n.type == NotificationType.REPLY || n.type == NotificationType.LIKE -> NotificationSection.OFFERS
    n.type == NotificationType.NEW_LISTING || n.type == NotificationType.VIEW -> NotificationSection.POSTS
    else -> NotificationSection.SYSTEM
}

private fun quotedPartOf(message: String): String? = Regex("\"([^\"]+)\"").find(message)?.groupValues?.getOrNull(1)

private fun offerMessageText(n: AppNotification): Pair<String, String> {
    val quoted = quotedPartOf(n.message)
    return when (n.type) {
        NotificationType.OFFER -> "${n.actor} wants to buy your item" to (quoted?.let { "\"$it\"" } ?: n.message)
        NotificationType.MESSAGE, NotificationType.REPLY -> "${n.actor} sent you a message" to n.message
        NotificationType.LIKE -> "${n.actor} liked your item" to (quoted?.let { "\"$it\"" } ?: n.message)
        else -> n.actor to n.message
    }
}

private fun postHeadline(n: AppNotification): String {
    val cat = n.category ?: "item"
    val kind = when {
        cat.equals("Rooms", true) -> "Rooms listing"
        cat.equals("Roommate", true) -> "Roommate request"
        else -> "$cat item"
    }
    return if (n.actor.isNotBlank() && !n.actor.equals("New Listing", true)) "${n.actor} posted a $kind" else "New $kind posted"
}

private fun postDetail(n: AppNotification): String = quotedPartOf(n.message)?.let { "\"$it\"" } ?: n.message

@Composable
private fun categoryVisual(category: String?): Triple<ImageVector, Color, Color> {
    val c = category?.lowercase().orEmpty()
    return when {
        c.contains("furniture") -> Triple(Icons.Default.Chair, IconGreen, PastelGreen)
        c.contains("electronics") -> Triple(Icons.Default.Devices, IconBlue, PastelBlue)
        c.contains("book") -> Triple(Icons.Default.Book, IconOrange, PastelPeach)
        c.contains("roommate") -> Triple(Icons.Default.Person, IconPink, PastelPink)
        c.contains("room") -> Triple(Icons.Default.VpnKey, IconPurple, PastelLavender)
        else -> Triple(Icons.Default.Storefront, IconBlue, PastelBlue)
    }
}

@Composable
private fun colorsForName(name: String): Pair<Color, Color> {
    val palette = listOf(
        PastelBlue to IconBlue,
        PastelGreen to IconGreen,
        PastelPeach to IconOrange,
        PastelLavender to IconPurple,
        PastelPink to IconPink
    )
    return palette[abs(name.hashCode()) % palette.size]
}

private fun initialsOf(name: String): String = name.trim().split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString("")

private fun safeRelativeTime(raw: String): String = runCatching { relativeTimeFrom(raw) }.getOrDefault(raw)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: UniqoRepository,
    onBack: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenChatWith: (AppNotification) -> Unit,
    onOpenMyListings: () -> Unit,
    onOpenListing: (String) -> Unit = {}
) {
    val notifications by repository.notifications.collectAsState()
    var activeFilter by remember { mutableStateOf("All") }
    var announcementDialog by remember { mutableStateOf<AppNotification?>(null) }
    var offersExpanded by remember { mutableStateOf(true) }
    var postsExpanded by remember { mutableStateOf(true) }
    var systemExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { repository.markNotificationsRead() }

    val filtered = if (activeFilter == "All") notifications else notifications.filter { notificationBucket(it) == activeFilter }
    val offers = filtered.filter { sectionOf(it) == NotificationSection.OFFERS }
    val posts = filtered.filter { sectionOf(it) == NotificationSection.POSTS }
    val system = filtered.filter { sectionOf(it) == NotificationSection.SYSTEM }

    announcementDialog?.let { notif ->
        AlertDialog(
            onDismissRequest = { announcementDialog = null },
            title = { Text(notif.actor, fontWeight = FontWeight.Bold) },
            text = { Text(notif.message) },
            confirmButton = { TextButton(onClick = { announcementDialog = null }) { Text("OK") } }
        )
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notificationFilterTabs) { label ->
                        FilterChip(label = label, selected = activeFilter == label, onClick = { activeFilter = label })
                    }
                }
            }

            if (offers.isNotEmpty()) {
                item {
                    NotifSectionCard {
                        NotifSectionHeader(Icons.Default.ChatBubble, PurplePrimary, PurpleLight, "Offers & Messages", offers.size, PurplePrimary, offersExpanded) { offersExpanded = !offersExpanded }
                        if (offersExpanded) {
                            Spacer(Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                offers.forEach { n ->
                                    OfferMessageCard(n, { onOpenChatWith(n) }) { n.relatedId?.let(onOpenListing) ?: onOpenChatWith(n) }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            SectionFooterLink("View all messages", PurplePrimary, onOpenChats)
                        }
                    }
                }
            }

            if (posts.isNotEmpty()) {
                item {
                    NotifSectionCard {
                        NotifSectionHeader(Icons.Default.Storefront, SuccessGreen, PastelGreen, "New Posts", posts.size, SuccessGreen, postsExpanded) { postsExpanded = !postsExpanded }
                        if (postsExpanded) {
                            Spacer(Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                posts.forEach { n -> PostRow(n) { n.relatedId?.let(onOpenListing) ?: onOpenMyListings() } }
                            }
                            Spacer(Modifier.height(6.dp))
                            SectionFooterLink("View all posts", SuccessGreen, onOpenMyListings)
                        }
                    }
                }
            }

            if (system.isNotEmpty()) {
                item {
                    NotifSectionCard {
                        NotifSectionHeader(Icons.Default.NotificationsActive, IconBlue, PastelBlue, "System Updates", system.size, IconBlue, systemExpanded) { systemExpanded = !systemExpanded }
                        if (systemExpanded) {
                            Spacer(Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                system.forEach { n ->
                                    SystemRow(n) {
                                        if (n.type == NotificationType.ANNOUNCEMENT) announcementDialog = n
                                        else n.relatedId?.let(onOpenListing)
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            SectionFooterLink("View all updates", IconBlue) {}
                        }
                    }
                }
            }

            if (offers.isEmpty() && posts.isEmpty() && system.isEmpty()) item {
                EmptyState("You're all caught up", "No notifications to show.")
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun NotifSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardWhite).border(1.dp, Divider, RoundedCornerShape(20.dp)).padding(14.dp),
        content = content
    )
}

@Composable
private fun NotifSectionHeader(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    count: Int,
    badgeColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
        Box(
            Modifier.clip(RoundedCornerShape(50)).background(badgeColor.copy(alpha = .15f)).padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text("$count", style = MaterialTheme.typography.labelMedium, color = badgeColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(4.dp))
        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = TextSecondary)
    }
}

@Composable
private fun SectionFooterLink(text: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = color, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = color, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun OfferMessageCard(notif: AppNotification, onPrimaryAction: () -> Unit, onClick: () -> Unit) {
    val (bg, accent) = colorsForName(notif.actor)
    val (headline, detail) = offerMessageText(notif)

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (notif.isUnread) PurpleLight else Background).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                Text(initialsOf(notif.actor), color = accent, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.size(18.dp).clip(CircleShape).background(PurplePrimary), contentAlignment = Alignment.Center) {
                Icon(
                    if (notif.type == NotificationType.MESSAGE || notif.type == NotificationType.REPLY) Icons.Default.ChatBubble else Icons.Default.LocalOffer,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(headline, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(safeRelativeTime(notif.time), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            when (notif.type) {
                NotificationType.MESSAGE, NotificationType.REPLY -> Text(
                    "View Chat →",
                    color = PurplePrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onPrimaryAction)
                )
                else -> Button(
                    onClick = onPrimaryAction,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Msg User", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PostRow(notif: AppNotification, onClick: () -> Unit) {
    val (icon, tint, bg) = categoryVisual(notif.category)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (notif.isUnread) PastelGreen.copy(alpha = .35f) else Background).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(postHeadline(notif), style = MaterialTheme.typography.bodyMedium, fontWeight = if (notif.isUnread) FontWeight.SemiBold else FontWeight.Normal, color = TextPrimary, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text("${postDetail(notif)} · ${safeRelativeTime(notif.time)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SystemRow(notif: AppNotification, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PastelBlue.copy(alpha = if (notif.isUnread) .45f else .2f)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(CardWhite), contentAlignment = Alignment.Center) {
            Icon(
                if (notif.type == NotificationType.ANNOUNCEMENT) Icons.Default.Campaign else Icons.Default.VerifiedUser,
                null,
                tint = IconBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(notif.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = if (notif.isUnread) FontWeight.Medium else FontWeight.Normal, maxLines = 2)
            Spacer(Modifier.height(2.dp))
            Text(safeRelativeTime(notif.time), style = MaterialTheme.typography.labelSmall, color = TextSecondary.copy(alpha = .8f))
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary.copy(alpha = .6f), modifier = Modifier.size(16.dp))
    }
}