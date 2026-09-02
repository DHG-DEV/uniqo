package com.example.uniqo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(repository: UniqoRepository, onConversationClick: (String) -> Unit) {
    val conversations by repository.conversations.collectAsState()
    var filter by remember { mutableStateOf(ChatFilter.ALL) }
    var query by remember { mutableStateOf("") }

    // Username search results — separate from the conversation list below.
    // Debounced so we're not hitting Supabase on every keystroke.
    var searchResults by remember { mutableStateOf<List<Student>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val isSearchMode = query.trim().length >= 2

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            searchResults = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(300) // debounce
        searchResults = repository.searchUsersByUsername(query.trim())
        searching = false
    }

    val filtered = conversations.filter {
        (filter == ChatFilter.ALL || (filter == ChatFilter.UNREAD && it.unreadCount > 0) || it.context == filter) &&
                (query.isBlank() || it.participant.name.contains(query, true))
    }

    Column(Modifier.fillMaxSize().background(Background).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Chat", style = MaterialTheme.typography.headlineSmall)
            Icon(Icons.Default.Search, "Search", tint = TextPrimary)
        }
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search messages or @username...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (isSearchMode) {
            // -----------------------------------------------------------
            // SEARCH MODE: show matching usernames instead of the chat list.
            // Tapping a result opens (or creates) a conversation with them.
            // -----------------------------------------------------------
            Spacer(Modifier.height(16.dp))
            Text(
                "People",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))

            when {
                searching -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = PurplePrimary)
                }
                searchResults.isEmpty() -> EmptyState("No users found", "Try a different username.")
                else -> LazyColumn {
                    items(searchResults, key = { it.id }) { user ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val convoId = repository.findOrCreateConversation(user.id, user.name, user.avatarUrl)
                                    query = ""
                                    onConversationClick(convoId)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(user.avatarUrl, 44)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
            return@Column
        }

        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(ChatFilter.ALL, ChatFilter.UNREAD, ChatFilter.ROOMS, ChatFilter.MARKETPLACE)) { f ->
                FilterChip(
                    label = f.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = filter == f, onClick = { filter = f }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) EmptyState("No conversations", "Start chatting from a listing, room, or by searching a username.")
        else LazyColumn {
            items(filtered, key = { it.id }) { convo ->
                var showPinMenu by remember { mutableStateOf(false) }

                Box {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (convo.isPinned) PurpleLight.copy(alpha = 0.25f) else Color.Transparent)
                            .combinedClickable(
                                onClick = {
                                    repository.markConversationRead(convo.id)
                                    onConversationClick(convo.id)
                                },
                                onLongClick = { showPinMenu = true }
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(convo.participant.avatarUrl, 48)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (convo.isPinned) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        tint = PurplePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    convo.participant.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (convo.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold
                                )
                            }
                            PulsePresence(convo.participant.lastActiveAt)
                            Text(
                                convo.lastMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (convo.unreadCount > 0) TextPrimary else TextSecondary,
                                fontWeight = if (convo.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                convo.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (convo.unreadCount > 0) PurplePrimary else TextSecondary
                            )
                            if (convo.unreadCount > 0) {
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier.size(18.dp).clip(CircleShape).background(PurplePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${convo.unreadCount}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    DropdownMenu(expanded = showPinMenu, onDismissRequest = { showPinMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (convo.isPinned) "Unpin chat" else "Pin chat") },
                            onClick = {
                                repository.togglePinConversation(convo.id)
                                showPinMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}