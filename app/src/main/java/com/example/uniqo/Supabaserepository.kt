package com.example.uniqo

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
private data class ConversationRow(
    val id: String,
    @SerialName("user_a_id") val userAId: String,
    @SerialName("user_b_id") val userBId: String,
    @SerialName("last_message") val lastMessage: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("user_a_last_read_at") val userALastReadAt: String? = null,
    @SerialName("user_b_last_read_at") val userBLastReadAt: String? = null,
    @SerialName("user_a_pinned") val userAPinned: Boolean = false,
    @SerialName("user_b_pinned") val userBPinned: Boolean = false
)

@Serializable
private data class NewConversationRow(
    val id: String,
    @SerialName("user_a_id") val userAId: String,
    @SerialName("user_b_id") val userBId: String
)

@Serializable
private data class ConversationLastMessageUpdate(
    @SerialName("last_message") val lastMessage: String,
    @SerialName("last_message_at") val lastMessageAt: String
)

@Serializable
private data class ConversationReadUpdate(
    @SerialName("user_a_last_read_at") val userALastReadAt: String? = null,
    @SerialName("user_b_last_read_at") val userBLastReadAt: String? = null
)

@Serializable
private data class ConversationPinUpdate(
    @SerialName("user_a_pinned") val userAPinned: Boolean? = null,
    @SerialName("user_b_pinned") val userBPinned: Boolean? = null
)

@Serializable
private data class MessageRow(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val text: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class NewMessageRow(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val text: String
)

@Serializable
private data class ProfileRow(
    val id: String,
    val name: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val college: String? = null,
    val course: String? = null,
    val year: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("student_verified") val studentVerified: Boolean = false,
    @SerialName("phone_verified") val phoneVerified: Boolean = false,
    val rating: Double = 5.0,
    val username: String? = null,
    @SerialName("mobile_number") val mobileNumber: String? = null,
    val bio: String? = null,
    val location: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
private data class ProfileUpdateRow(
    val name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    @SerialName("mobile_number") val mobileNumber: String? = null,
    val location: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
private data class ListingRow(
    val id: String,
    val title: String,
    val price: Int,
    val category: String,
    @SerialName("sub_category") val subCategory: String? = null,
    val condition: String,
    @SerialName("distance_km") val distanceKm: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    val description: String? = null,
    @SerialName("seller_id") val sellerId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
private data class RoomRow(
    val id: String,
    val title: String,
    @SerialName("rent_per_month") val rentPerMonth: Int,
    val type: String,
    val furnishing: String,
    @SerialName("food_included") val foodIncluded: Boolean = false,
    @SerialName("distance_km") val distanceKm: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
private data class FavoriteRow(
    @SerialName("user_id") val userId: String,
    @SerialName("listing_id") val listingId: String
)

@Serializable
private data class NewListingRow(
    val title: String,
    val price: Int,
    val category: String,
    @SerialName("sub_category") val subCategory: String?,
    val condition: String,
    val description: String?,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("seller_id") val sellerId: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?
)

@Serializable
private data class AnnouncementRow(
    val id: String,
    val title: String,
    val message: String,
    val category: String = "General",
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class AnnouncementReadRow(
    @SerialName("announcement_id") val announcementId: String,
    @SerialName("user_id") val userId: String
)

@Serializable
private data class ListingNotificationRow(
    val id: String,
    @SerialName("listing_id") val listingId: String,
    @SerialName("seller_id") val sellerId: String,
    val title: String,
    val category: String? = null,
    val price: Int? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class ListingNotificationReadRow(
    @SerialName("listing_notification_id") val listingNotificationId: String,
    @SerialName("user_id") val userId: String
)

@Serializable
private data class OfferNotificationRow(
    val id: String,
    @SerialName("listing_id") val listingId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("buyer_name") val buyerName: String,
    @SerialName("buyer_avatar_url") val buyerAvatarUrl: String? = null,
    @SerialName("listing_title") val listingTitle: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class NewOfferNotificationRow(
    @SerialName("listing_id") val listingId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("buyer_name") val buyerName: String,
    @SerialName("buyer_avatar_url") val buyerAvatarUrl: String?,
    @SerialName("listing_title") val listingTitle: String
)

@Serializable
private data class OfferNotificationReadRow(
    @SerialName("offer_notification_id") val offerNotificationId: String,
    @SerialName("user_id") val userId: String
)

data class ChatMediaUploadResult(
    val url: String,
    val path: String,
    val fileName: String,
    val mimeType: String
)

class SupabaseRepository : UniqoRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db get() = SupabaseClient.client.postgrest
    private var cachedUser: Student = MockData.currentUser

    private val pendingConversationIds =
        java.util.Collections.synchronizedSet(mutableSetOf<String>())

    override val listings = MutableStateFlow<List<Listing>>(emptyList())
    override val roomListings = MutableStateFlow<List<RoomListing>>(emptyList())
    override val favorites = MutableStateFlow<Set<String>>(emptySet())
    override val activeFilters = MutableStateFlow(ListingFilters())
    override val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val myPreferences = MutableStateFlow(MockData.myPreferences)
    override val notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    override val currentUserProfile = MutableStateFlow(MockData.currentUser)
    override val lookingForPreferences = MutableStateFlow<List<LookingForPreference>>(emptyList())

    init {
        scope.launch { refreshCurrentUser() }
        scope.launch { refreshListings() }
        scope.launch { refreshRooms() }
        scope.launch { refreshFavorites() }
        scope.launch { refreshNotifications() }
        scope.launch { refreshConversations() }
        subscribeToMessages()
        subscribeToListings()
        startMessagePolling()
        startListingPolling()
    }

    suspend fun refreshCurrentUser() {
        try {
            val userId = AuthManager.currentUserId() ?: return
            val row = db["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<ProfileRow>() ?: return
            val sessionEmail =
                SupabaseClient.client.auth.currentUserOrNull()?.email ?: ""
            val student = row.toStudent(sessionEmail)
            cachedUser = student
            currentUserProfile.value = student
        } catch (e: Exception) {
            Log.e("UNIQO_USER", "Failed to refresh current user", e)
        }
    }

    suspend fun onAuthenticated() {
        refreshCurrentUser()
        refreshListings()
        refreshFavorites()
        refreshNotifications()
        refreshConversations()
    }

    override fun currentUser(): Student = currentUserProfile.value

    override fun updateProfile(edits: ProfileEdits) {
        val userId = AuthManager.currentUserId() ?: return
        currentUserProfile.update { current ->
            current.copy(
                name = edits.name ?: current.name,
                username = edits.username ?: current.username,
                bio = edits.bio ?: current.bio,
                phone = edits.phone ?: current.phone,
                location = edits.location ?: current.location,
                avatarUrl = edits.avatarUrl ?: current.avatarUrl
            )
        }
        cachedUser = currentUserProfile.value

        scope.launch {
            try {
                db["profiles"].update(
                    ProfileUpdateRow(
                        name = edits.name,
                        username = edits.username,
                        bio = edits.bio,
                        mobileNumber = edits.phone,
                        location = edits.location,
                        avatarUrl = edits.avatarUrl
                    )
                ) { filter { eq("id", userId) } }

                if (edits.email != null) {
                    SupabaseClient.client.auth.updateUser { email = edits.email }
                }
            } catch (e: Exception) {
                Log.e("UNIQO_PROFILE", "Profile update failed", e)
            }
        }
    }

    suspend fun uploadProfilePhoto(
        imageBytes: ByteArray,
        fileExtension: String = "jpg"
    ): Result<String> = runCatching {
        val userId = AuthManager.currentUserId() ?: error("Not signed in")
        val path = "$userId/${System.currentTimeMillis()}.$fileExtension"
        SupabaseClient.client.storage["profile-images"].upload(path, imageBytes)
        SupabaseClient.client.storage["profile-images"].publicUrl(path)
    }

    override suspend fun searchUsersByUsername(query: String): List<Student> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val myId = AuthManager.currentUserId()
        return try {
            db["profiles"].select {
                filter { ilike("username", "%$trimmed%") }
                limit(20)
            }.decodeList<ProfileRow>()
                .filter { it.id != myId && !it.username.isNullOrBlank() }
                .map { it.toStudent() }
        } catch (e: Exception) {
            Log.e("UNIQO_SEARCH", "Username search failed", e)
            emptyList()
        }
    }

    private suspend fun refreshListings() {
        try {
            Log.d("UNIQO_LISTINGS", "Fetching global listings")

            val rows = db["listings"].select {
                order("created_at", Order.DESCENDING)
            }.decodeList<ListingRow>()

            val sellerIds = rows.mapNotNull { it.sellerId }.distinct()
            val sellers = fetchProfiles(sellerIds)

            listings.value = rows.map { row ->
                val seller = row.sellerId?.let { sellers[it] }
                    ?: Student(
                        id = row.sellerId ?: "",
                        name = "Unknown",
                        avatarUrl = "",
                        college = "",
                        course = "",
                        year = ""
                    )
                row.toListing(seller)
            }

            Log.d(
                "UNIQO_LISTINGS",
                "Global listings loaded: ${listings.value.size}"
            )
        } catch (e: Exception) {
            Log.e("UNIQO_LISTINGS", "Global listings fetch failed", e)
        }
    }

    private suspend fun refreshRooms() {
        try {
            val rows = db["rooms"].select().decodeList<RoomRow>()
            val ownerIds = rows.mapNotNull { it.ownerId }.distinct()
            val owners = fetchProfiles(ownerIds)
            roomListings.value =
                rows.map { it.toRoomListing(owners[it.ownerId] ?: cachedUser) }
        } catch (e: Exception) {
            Log.e("UNIQO_ROOMS", "Failed to refresh rooms", e)
        }
    }

    private suspend fun fetchProfiles(ids: List<String>): Map<String, Student> {
        if (ids.isEmpty()) return emptyMap()
        return try {
            db["profiles"].select {
                filter { isIn("id", ids) }
            }.decodeList<ProfileRow>().associate { it.id to it.toStudent() }
        } catch (e: Exception) {
            Log.e("UNIQO_PROFILES", "Failed to fetch profiles", e)
            emptyMap()
        }
    }

    private suspend fun fetchProfile(id: String): Student? {
        return try {
            db["profiles"].select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<ProfileRow>()?.toStudent()
        } catch (e: Exception) {
            Log.e("UNIQO_PROFILE", "Failed to fetch profile", e)
            null
        }
    }

    private suspend fun refreshFavorites() {
        try {
            val userId = AuthManager.currentUserId() ?: return
            val rows = db["favorites"].select {
                filter { eq("user_id", userId) }
            }.decodeList<FavoriteRow>()
            favorites.value = rows.map { it.listingId }.toSet()
        } catch (e: Exception) {
            Log.e("UNIQO_FAVORITES", "Failed to refresh favorites", e)
        }
    }

    override fun toggleFavorite(listingId: String) {
        val userId = AuthManager.currentUserId() ?: return
        val currentlyFavorite = favorites.value.contains(listingId)
        favorites.update { current ->
            if (currentlyFavorite) current - listingId else current + listingId
        }
        scope.launch {
            try {
                if (currentlyFavorite) {
                    db["favorites"].delete {
                        filter {
                            eq("user_id", userId)
                            eq("listing_id", listingId)
                        }
                    }
                } else {
                    db["favorites"].insert(
                        FavoriteRow(userId = userId, listingId = listingId)
                    )
                }
            } catch (e: Exception) {
                Log.e("UNIQO_FAVORITES", "Favorite update failed", e)
                refreshFavorites()
            }
        }
    }

    override fun isFavorite(listingId: String): Boolean =
        favorites.value.contains(listingId)

    private suspend fun refreshNotifications() {
        try {
            val userId = AuthManager.currentUserId() ?: return

            val announcementRows =
                db["announcements"].select().decodeList<AnnouncementRow>()

            val announcementReadIds =
                db["announcement_reads"].select {
                    filter { eq("user_id", userId) }
                }.decodeList<AnnouncementReadRow>()
                    .map { it.announcementId }
                    .toSet()

            val announcementNotifs =
                announcementRows.sortedByDescending { it.createdAt }.map { row ->
                    AppNotification(
                        id = "ann_${row.id}",
                        actor = row.title,
                        avatarUrl = "",
                        message = row.message,
                        time = row.createdAt,
                        type = NotificationType.ANNOUNCEMENT,
                        isUnread = row.id !in announcementReadIds,
                        category = row.category
                    )
                }

            val listingNotifRows =
                db["listing_notifications"].select()
                    .decodeList<ListingNotificationRow>()

            val listingReadIds =
                db["listing_notification_reads"].select {
                    filter { eq("user_id", userId) }
                }.decodeList<ListingNotificationReadRow>()
                    .map { it.listingNotificationId }
                    .toSet()

            val listingNotifs =
                listingNotifRows
                    .filter { it.sellerId != userId }
                    .sortedByDescending { it.createdAt }
                    .map { row ->
                        val priceText = row.price?.let { " for ₹$it" } ?: ""
                        AppNotification(
                            id = "lst_${row.id}",
                            actor = "New Listing",
                            avatarUrl = "",
                            message =
                                "\"${row.title}\" was just listed$priceText",
                            time = row.createdAt,
                            type = NotificationType.NEW_LISTING,
                            isUnread = row.id !in listingReadIds,
                            category = row.category,
                            relatedId = row.listingId,
                            actorId = row.sellerId
                        )
                    }

            val offerNotifRows =
                db["offer_notifications"].select {
                    filter { eq("seller_id", userId) }
                }.decodeList<OfferNotificationRow>()

            val offerReadIds =
                db["offer_notification_reads"].select {
                    filter { eq("user_id", userId) }
                }.decodeList<OfferNotificationReadRow>()
                    .map { it.offerNotificationId }
                    .toSet()

            val offerNotifs =
                offerNotifRows.sortedByDescending { it.createdAt }.map { row ->
                    AppNotification(
                        id = "off_${row.id}",
                        actor = row.buyerName,
                        avatarUrl = row.buyerAvatarUrl ?: "",
                        message =
                            "${row.buyerName} wants to buy your listing \"${row.listingTitle}\"",
                        time = row.createdAt,
                        type = NotificationType.OFFER,
                        isUnread = row.id !in offerReadIds,
                        relatedId = row.listingId,
                        actorId = row.buyerId
                    )
                }

            notifications.update { current ->
                val untouched = current.filterNot {
                    it.id.startsWith("ann_") ||
                            it.id.startsWith("lst_") ||
                            it.id.startsWith("off_")
                }
                announcementNotifs + listingNotifs + offerNotifs + untouched
            }
        } catch (e: Exception) {
            Log.e("UNIQO_NOTIFICATIONS", "Failed to refresh notifications", e)
        }
    }

    private fun addPostSuccessNotification(listing: Listing) {
        val notification = AppNotification(
            id = "selfpost_${listing.id}",
            actor = "Marketplace",
            avatarUrl = "",
            message =
                "Your listing \"${listing.title}\" was posted successfully!",
            time = "Just now",
            type = NotificationType.NEW_LISTING,
            isUnread = true,
            category = listing.category.label,
            relatedId = listing.id
        )
        notifications.update { listOf(notification) + it }
    }

    override fun markNotificationsRead() {
        val unread = notifications.value.filter { it.isUnread }
        val announcementIds =
            unread.filter { it.id.startsWith("ann_") }
                .map { it.id.removePrefix("ann_") }
        val listingIds =
            unread.filter { it.id.startsWith("lst_") }
                .map { it.id.removePrefix("lst_") }
        val offerIds =
            unread.filter { it.id.startsWith("off_") }
                .map { it.id.removePrefix("off_") }

        notifications.update { list ->
            list.map { it.copy(isUnread = false) }
        }

        val userId = AuthManager.currentUserId() ?: return

        scope.launch {
            try {
                announcementIds.forEach {
                    db["announcement_reads"].upsert(
                        AnnouncementReadRow(
                            announcementId = it,
                            userId = userId
                        )
                    )
                }
                listingIds.forEach {
                    db["listing_notification_reads"].upsert(
                        ListingNotificationReadRow(
                            listingNotificationId = it,
                            userId = userId
                        )
                    )
                }
                offerIds.forEach {
                    db["offer_notification_reads"].upsert(
                        OfferNotificationReadRow(
                            offerNotificationId = it,
                            userId = userId
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_NOTIFICATIONS",
                    "Failed to mark notifications read",
                    e
                )
            }
        }
    }

    override fun sendOffer(listing: Listing) {
        val buyerId = AuthManager.currentUserId() ?: return
        val buyer = cachedUser

        scope.launch {
            try {
                db["offer_notifications"].insert(
                    NewOfferNotificationRow(
                        listingId = listing.id,
                        sellerId = listing.seller.id,
                        buyerId = buyerId,
                        buyerName = buyer.name,
                        buyerAvatarUrl = buyer.avatarUrl,
                        listingTitle = listing.title
                    )
                )
            } catch (e: Exception) {
                Log.e("UNIQO_OFFER", "Offer failed", e)
            }
        }
    }

    override fun addListing(listing: Listing) {
        val userId = AuthManager.currentUserId() ?: return

        scope.launch {
            try {
                val row = NewListingRow(
                    title = listing.title.trim(),
                    price = listing.price,
                    category = listing.category.name,
                    subCategory = listing.subCategory.ifBlank { null },
                    condition = listing.condition.name,
                    description = listing.description.trim().ifBlank { null },
                    imageUrl = listing.imageUrl,
                    sellerId = userId,
                    latitude = listing.latitude,
                    longitude = listing.longitude,
                    address = listing.address?.trim()?.ifBlank { null }
                )

                Log.d(
                    "UNIQO_LISTINGS",
                    "Inserting listing: title=${row.title}, seller=$userId"
                )

                db["listings"].insert(row)

                Log.d(
                    "UNIQO_LISTINGS",
                    "Listing inserted successfully: ${row.title}"
                )

                refreshListings()
                refreshNotifications()
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_LISTINGS",
                    "LISTING INSERT FAILED: ${e.message}",
                    e
                )
            }
        }
    }

    suspend fun uploadListingPhotoAndPublish(
        listing: Listing,
        imageBytes: ByteArray?,
        fileExtension: String = "jpg"
    ): Result<Unit> = runCatching {
        val userId = AuthManager.currentUserId()
            ?: error("Not signed in")

        var imageUrl: String? = listing.imageUrl

        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val safeExtension =
                fileExtension.lowercase().replace(Regex("[^a-z0-9]"), "")

            val path =
                "$userId/${System.currentTimeMillis()}.$safeExtension"

            SupabaseClient.client.storage["listing-images"]
                .upload(path, imageBytes)

            imageUrl =
                SupabaseClient.client.storage["listing-images"]
                    .publicUrl(path)
        }

        val finalListing = listing.copy(imageUrl = imageUrl)

        val row = NewListingRow(
            title = finalListing.title.trim(),
            price = finalListing.price,
            category = finalListing.category.name,
            subCategory = finalListing.subCategory.ifBlank { null },
            condition = finalListing.condition.name,
            description =
                finalListing.description.trim().ifBlank { null },
            imageUrl = finalListing.imageUrl,
            sellerId = userId,
            latitude = finalListing.latitude,
            longitude = finalListing.longitude,
            address = finalListing.address?.trim()?.ifBlank { null }
        )

        Log.d(
            "UNIQO_LISTINGS",
            "Publishing listing: ${row.title}, seller=$userId"
        )

        db["listings"].insert(row)

        Log.d(
            "UNIQO_LISTINGS",
            "Listing published successfully: ${row.title}"
        )

        refreshListings()
        refreshNotifications()
        addPostSuccessNotification(finalListing)
    }

    private suspend fun refreshConversations() {
        try {
            val userId = AuthManager.currentUserId() ?: return

            val rows = db["conversations"].select {
                filter {
                    or {
                        eq("user_a_id", userId)
                        eq("user_b_id", userId)
                    }
                }
            }.decodeList<ConversationRow>()

            rows.forEach { pendingConversationIds.remove(it.id) }

            val otherIds = rows.map {
                if (it.userAId == userId) it.userBId else it.userAId
            }.toMutableSet()

            val pending =
                conversations.value.filter {
                    it.id in pendingConversationIds
                }

            otherIds.addAll(pending.map { it.participant.id })

            val others = fetchProfiles(otherIds.toList())
            val conversationIds = rows.map { it.id }

            val messageRows =
                if (conversationIds.isEmpty()) {
                    emptyList()
                } else {
                    db["messages"].select {
                        filter { isIn("conversation_id", conversationIds) }
                        order("created_at", Order.ASCENDING)
                    }.decodeList<MessageRow>()
                }

            val messagesByConversation =
                messageRows.groupBy { it.conversationId }

            val sortedRows =
                rows.sortedByDescending {
                    it.lastMessageAt ?: it.createdAt ?: ""
                }

            val mapped = sortedRows.map { row ->
                val otherId =
                    if (row.userAId == userId) row.userBId else row.userAId

                val other =
                    others[otherId] ?: Student(
                        id = otherId,
                        name = "Unknown",
                        avatarUrl = "",
                        college = "",
                        course = "",
                        year = ""
                    )

                val myLastReadAt =
                    if (row.userAId == userId)
                        row.userALastReadAt
                    else
                        row.userBLastReadAt

                val isPinned =
                    if (row.userAId == userId)
                        row.userAPinned
                    else
                        row.userBPinned

                val rawMessagesForConvo =
                    messagesByConversation[row.id] ?: emptyList()

                val unreadCount =
                    rawMessagesForConvo.count { m ->
                        m.senderId != userId &&
                                (myLastReadAt == null ||
                                        isAfter(m.createdAt, myLastReadAt))
                    }

                val messages =
                    rawMessagesForConvo.map { message ->
                        buildChatMessage(
                            row = message,
                            isMine = message.senderId == userId,
                            timestamp =
                                relativeTimeFrom(message.createdAt)
                        )
                    }

                Conversation(
                    id = row.id,
                    participant = other,
                    lastMessage =
                        row.lastMessage
                            ?: messages.lastOrNull()?.text
                            ?: "",
                    time =
                        row.lastMessageAt?.let {
                            relativeTimeFrom(it)
                        } ?: "",
                    unreadCount = unreadCount,
                    context = ChatFilter.MARKETPLACE,
                    messages = messages,
                    isPinned = isPinned
                )
            }

            val serverIds = mapped.map { it.id }.toSet()
            val pendingLocal =
                pending.filter { it.id !in serverIds }

            conversations.value =
                (mapped + pendingLocal)
                    .distinctBy { it.id }
                    .sortedWith(
                        compareByDescending<Conversation> { it.isPinned }
                            .thenByDescending { it.time == "Now" }
                    )
        } catch (e: Exception) {
            Log.e("UNIQO_CHAT", "Failed to refresh conversations", e)
        }
    }

    override fun sendMessage(conversationId: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        val userId = AuthManager.currentUserId() ?: return

        val optimisticMessage = ChatMessage(
            id = "temp_${System.currentTimeMillis()}",
            senderId = userId,
            text = cleanText,
            timestamp = "Now",
            isMine = true
        )

        conversations.update { list ->
            list.map { conversation ->
                if (conversation.id == conversationId) {
                    conversation.copy(
                        messages =
                            conversation.messages + optimisticMessage,
                        lastMessage = cleanText,
                        time = "Now"
                    )
                } else {
                    conversation
                }
            }
        }

        scope.launch {
            try {
                db["messages"].insert(
                    NewMessageRow(
                        conversationId = conversationId,
                        senderId = userId,
                        text = cleanText
                    )
                )
                updateConversationLastMessage(
                    conversationId,
                    cleanText
                )
            } catch (e: Exception) {
                Log.e("UNIQO_CHAT", "Message send failed", e)
            }
        }
    }

    override fun sendAttachmentMessage(
        conversationId: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ) {
        val userId = AuthManager.currentUserId() ?: return
        if (bytes.isEmpty()) return

        val tempId = "temp_${System.currentTimeMillis()}"

        val optimisticMessage = ChatMessage(
            id = tempId,
            senderId = userId,
            text = "",
            timestamp = "Now",
            isMine = true,
            messageType = messageTypeForMime(mimeType),
            fileName = fileName,
            fileUrl = null,
            fileMimeType = mimeType
        )

        conversations.update { list ->
            list.map { c ->
                if (c.id == conversationId) {
                    c.copy(
                        messages =
                            c.messages + optimisticMessage,
                        lastMessage = "📎 $fileName",
                        time = "Now"
                    )
                } else {
                    c
                }
            }
        }

        scope.launch {
            try {
                val upload =
                    uploadChatMedia(
                        bytes,
                        fileName,
                        mimeType
                    ).getOrThrow()

                val attachmentText = buildString {
                    append("[ATTACHMENT]\n")
                    append("name=")
                    append(upload.fileName)
                    append("\n")
                    append("type=")
                    append(upload.mimeType)
                    append("\n")
                    append("url=")
                    append(upload.url)
                }

                db["messages"].insert(
                    NewMessageRow(
                        conversationId = conversationId,
                        senderId = userId,
                        text = attachmentText
                    )
                )

                updateConversationLastMessage(
                    conversationId,
                    "📎 ${upload.fileName}"
                )

                refreshConversations()
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_CHAT",
                    "Attachment send failed",
                    e
                )
            }
        }
    }

    private suspend fun updateConversationLastMessage(
        conversationId: String,
        text: String
    ) {
        db["conversations"].update(
            ConversationLastMessageUpdate(
                lastMessage = text,
                lastMessageAt = Instant.now().toString()
            )
        ) {
            filter { eq("id", conversationId) }
        }
    }

    override fun markConversationRead(conversationId: String) {
        val userId = AuthManager.currentUserId() ?: return

        conversations.update { list ->
            list.map {
                if (it.id == conversationId)
                    it.copy(unreadCount = 0)
                else
                    it
            }
        }

        scope.launch {
            try {
                val row =
                    db["conversations"].select {
                        filter { eq("id", conversationId) }
                    }.decodeSingleOrNull<ConversationRow>()
                        ?: return@launch

                val nowIso = Instant.now().toString()

                val update =
                    if (row.userAId == userId) {
                        ConversationReadUpdate(
                            userALastReadAt = nowIso
                        )
                    } else {
                        ConversationReadUpdate(
                            userBLastReadAt = nowIso
                        )
                    }

                db["conversations"].update(update) {
                    filter { eq("id", conversationId) }
                }
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_CHAT",
                    "Mark read failed",
                    e
                )
            }
        }
    }

    override fun togglePinConversation(conversationId: String) {
        val userId = AuthManager.currentUserId() ?: return

        val current =
            conversations.value.firstOrNull {
                it.id == conversationId
            } ?: return

        val newPinned = !current.isPinned

        conversations.update { list ->
            list.map {
                if (it.id == conversationId)
                    it.copy(isPinned = newPinned)
                else
                    it
            }.sortedByDescending { it.isPinned }
        }

        scope.launch {
            try {
                val row =
                    db["conversations"].select {
                        filter { eq("id", conversationId) }
                    }.decodeSingleOrNull<ConversationRow>()
                        ?: return@launch

                val update =
                    if (row.userAId == userId) {
                        ConversationPinUpdate(
                            userAPinned = newPinned
                        )
                    } else {
                        ConversationPinUpdate(
                            userBPinned = newPinned
                        )
                    }

                db["conversations"].update(update) {
                    filter { eq("id", conversationId) }
                }
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_CHAT",
                    "Pin update failed",
                    e
                )
            }
        }
    }

    private fun subscribeToMessages() {
        scope.launch {
            try {
                val myId = AuthManager.currentUserId()
                val channel =
                    SupabaseClient.client.realtime
                        .channel("messages-changes")

                val changes =
                    channel.postgresChangeFlow<PostgresAction.Insert>(
                        schema = "public"
                    ) {
                        table = "messages"
                    }

                channel.subscribe()

                changes.collect { action ->
                    val row = action.decodeRecord<MessageRow>()

                    if (row.senderId == myId)
                        return@collect

                    conversations.update { list ->
                        list.map { conversation ->
                            if (conversation.id == row.conversationId) {
                                val alreadyExists =
                                    conversation.messages.any {
                                        it.id == row.id
                                    }

                                if (alreadyExists) {
                                    conversation
                                } else {
                                    val message =
                                        buildChatMessage(
                                            row = row,
                                            isMine = false,
                                            timestamp = "Now"
                                        )

                                    conversation.copy(
                                        messages =
                                            conversation.messages + message,
                                        lastMessage =
                                            if (message.messageType != "text")
                                                "📎 ${message.fileName}"
                                            else
                                                message.text,
                                        time = "Now",
                                        unreadCount =
                                            conversation.unreadCount + 1
                                    )
                                }
                            } else {
                                conversation
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_CHAT",
                    "Realtime unavailable",
                    e
                )
            }
        }
    }

    private fun subscribeToListings() {
        scope.launch {
            try {
                val channel =
                    SupabaseClient.client.realtime
                        .channel("listings-changes")

                val inserts =
                    channel.postgresChangeFlow<PostgresAction.Insert>(
                        schema = "public"
                    ) {
                        table = "listings"
                    }

                val updates =
                    channel.postgresChangeFlow<PostgresAction.Update>(
                        schema = "public"
                    ) {
                        table = "listings"
                    }

                val deletes =
                    channel.postgresChangeFlow<PostgresAction.Delete>(
                        schema = "public"
                    ) {
                        table = "listings"
                    }

                channel.subscribe()

                launch {
                    inserts.collect {
                        refreshListings()
                    }
                }

                launch {
                    updates.collect {
                        refreshListings()
                    }
                }

                launch {
                    deletes.collect {
                        refreshListings()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "UNIQO_LISTINGS",
                    "Listings realtime unavailable",
                    e
                )
            }
        }
    }

    private fun startMessagePolling() {
        scope.launch {
            while (isActive) {
                delay(1000.milliseconds)
                try {
                    if (AuthManager.currentUserId() != null) {
                        refreshConversations()
                    }
                } catch (e: Exception) {
                    Log.e(
                        "UNIQO_CHAT",
                        "Polling failed",
                        e
                    )
                }
            }
        }
    }

    private fun startListingPolling() {
        scope.launch {
            while (isActive) {
                delay(3000.milliseconds)
                try {
                    if (AuthManager.currentUserId() != null) {
                        refreshListings()
                    }
                } catch (e: Exception) {
                    Log.e(
                        "UNIQO_LISTINGS",
                        "Listing polling failed",
                        e
                    )
                }
            }
        }
    }

    override fun findOrCreateConversation(
        userId: String,
        name: String,
        avatarUrl: String
    ): String {
        val cleanUserId = userId.trim()
        if (cleanUserId.isEmpty()) return ""

        conversations.value.firstOrNull {
            it.participant.id == cleanUserId
        }?.let { return it.id }

        val myId = AuthManager.currentUserId()

        if (myId.isNullOrBlank() || myId == cleanUserId)
            return ""

        val placeholder = Student(
            id = cleanUserId,
            name = name,
            avatarUrl = avatarUrl,
            college = "",
            course = "",
            year = ""
        )

        val conversationId = UUID.randomUUID().toString()

        val conversation = Conversation(
            id = conversationId,
            participant = placeholder,
            lastMessage = "",
            time = "Now",
            unreadCount = 0,
            context = ChatFilter.MARKETPLACE,
            messages = emptyList()
        )

        pendingConversationIds.add(conversationId)

        conversations.update {
            listOf(conversation) + it
        }

        scope.launch {
            try {
                val (orderedA, orderedB) =
                    if (myId < cleanUserId)
                        myId to cleanUserId
                    else
                        cleanUserId to myId

                val existing =
                    db["conversations"].select {
                        filter {
                            or {
                                and {
                                    eq("user_a_id", orderedA)
                                    eq("user_b_id", orderedB)
                                }
                                and {
                                    eq("user_a_id", orderedB)
                                    eq("user_b_id", orderedA)
                                }
                            }
                        }
                        limit(1)
                    }.decodeSingleOrNull<ConversationRow>()

                if (existing != null) {
                    pendingConversationIds.remove(conversationId)

                    conversations.update {
                        it.filterNot {
                            it.id == conversationId
                        }
                    }

                    refreshConversations()
                    return@launch
                }

                db["conversations"].insert(
                    NewConversationRow(
                        id = conversationId,
                        userAId = orderedA,
                        userBId = orderedB
                    )
                )

                var confirmed = false

                repeat(10) {
                    val serverRow =
                        db["conversations"].select {
                            filter {
                                eq("id", conversationId)
                            }
                            limit(1)
                        }.decodeSingleOrNull<ConversationRow>()

                    if (serverRow != null) {
                        confirmed = true
                        return@repeat
                    }

                    delay(100)
                }

                if (confirmed) {
                    pendingConversationIds.remove(
                        conversationId
                    )
                }

                val fullProfile =
                    fetchProfile(cleanUserId)

                if (fullProfile != null) {
                    conversations.update { list ->
                        list.map {
                            if (it.id == conversationId)
                                it.copy(
                                    participant = fullProfile
                                )
                            else
                                it
                        }
                    }
                }

                refreshConversations()
            } catch (e: Exception) {
                pendingConversationIds.remove(
                    conversationId
                )

                conversations.update {
                    it.filterNot {
                        it.id == conversationId
                    }
                }

                Log.e(
                    "UNIQO_CHAT",
                    "Conversation creation failed",
                    e
                )
            }
        }

        return conversationId
    }

    suspend fun uploadChatMedia(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<ChatMediaUploadResult> = runCatching {
        val userId =
            AuthManager.currentUserId()
                ?: error("Not signed in")

        require(bytes.isNotEmpty()) {
            "File is empty"
        }

        val safeName =
            fileName.replace(
                Regex("[^A-Za-z0-9._-]"),
                "_"
            )

        val folder =
            when {
                mimeType.startsWith("image/") ->
                    "images"

                mimeType.startsWith("video/") ->
                    "videos"

                else ->
                    "files"
            }

        val uniqueName =
            "${System.currentTimeMillis()}_$safeName"

        val path =
            "$userId/$folder/$uniqueName"

        SupabaseClient.client
            .storage["chat-media"]
            .upload(path, bytes)

        val url =
            SupabaseClient.client
                .storage["chat-media"]
                .publicUrl(path)

        ChatMediaUploadResult(
            url = url,
            path = path,
            fileName = safeName,
            mimeType = mimeType
        )
    }

    suspend fun uploadChatImage(
        bytes: ByteArray,
        fileName: String = "image.jpg"
    ): Result<ChatMediaUploadResult> =
        uploadChatMedia(
            bytes,
            fileName,
            "image/jpeg"
        )

    suspend fun uploadChatVideo(
        bytes: ByteArray,
        fileName: String = "video.mp4"
    ): Result<ChatMediaUploadResult> =
        uploadChatMedia(
            bytes,
            fileName,
            "video/mp4"
        )

    suspend fun uploadChatFile(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<ChatMediaUploadResult> =
        uploadChatMedia(
            bytes,
            fileName,
            mimeType
        )

    suspend fun deleteChatMedia(
        path: String
    ): Result<Unit> = runCatching {
        SupabaseClient.client
            .storage["chat-media"]
            .delete(listOf(path))
    }

    override fun updatePreferences(
        preferences: RoommatePreferences
    ) {
        myPreferences.value = preferences
    }

    override fun roommateCandidates(): List<RoommateCandidate> =
        MockData.roommateCandidates

    override fun setCategoryFilter(
        category: ListingCategory?
    ) {
        activeFilters.update {
            it.copy(category = category)
        }
    }

    override fun updateFilters(
        filters: ListingFilters
    ) {
        activeFilters.value = filters
    }

    override fun addLookingForPreference(
        category: LookingForCategory,
        fields: Map<String, String>
    ) {
        val pref = LookingForPreference(
            id = UUID.randomUUID().toString(),
            category = category,
            isActive = true,
            fields = fields,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString()
        )
        lookingForPreferences.update { listOf(pref) + it }
    }

    override fun updateLookingForPreference(
        id: String,
        fields: Map<String, String>
    ) {
        lookingForPreferences.update { list ->
            list.map {
                if (it.id == id) it.copy(
                    fields = fields,
                    updatedAt = Instant.now().toString()
                ) else it
            }
        }
    }

    override fun deactivateLookingForPreference(id: String) {
        lookingForPreferences.update { list ->
            list.filterNot { it.id == id }
        }
    }

    override suspend fun getUserProfile(userId: String): Student? {
        return fetchProfile(userId)
    }
}

private fun isAfter(
    a: String,
    b: String
): Boolean =
    try {
        Instant.parse(a) > Instant.parse(b)
    } catch (e: Exception) {
        true
    }

private fun buildChatMessage(
    row: MessageRow,
    isMine: Boolean,
    timestamp: String
): ChatMessage {
    val text = row.text

    if (!text.startsWith("[ATTACHMENT]")) {
        return ChatMessage(
            id = row.id,
            senderId = row.senderId,
            text = text,
            timestamp = timestamp,
            isMine = isMine
        )
    }

    var name = ""
    var type = ""
    var url = ""

    text.lineSequence()
        .drop(1)
        .forEach { line ->
            when {
                line.startsWith("name=") ->
                    name = line.removePrefix("name=")

                line.startsWith("type=") ->
                    type = line.removePrefix("type=")

                line.startsWith("url=") ->
                    url = line.removePrefix("url=")
            }
        }

    return if (url.isBlank()) {
        ChatMessage(
            id = row.id,
            senderId = row.senderId,
            text = text,
            timestamp = timestamp,
            isMine = isMine
        )
    } else {
        ChatMessage(
            id = row.id,
            senderId = row.senderId,
            text = "",
            timestamp = timestamp,
            isMine = isMine,
            messageType = messageTypeForMime(type),
            fileName = name,
            fileUrl = url,
            fileMimeType = type
        )
    }
}

private fun messageTypeForMime(
    mimeType: String
): String =
    when {
        mimeType.startsWith("image/") ->
            "image"

        mimeType.startsWith("video/") ->
            "video"

        mimeType == "application/pdf" ->
            "pdf"

        else ->
            "file"
    }

private fun relativeTimeFrom(
    timestamp: String
): String =
    try {
        val instant = Instant.parse(timestamp)
        val seconds =
            (Instant.now().epochSecond -
                    instant.epochSecond)
                .coerceAtLeast(0)

        when {
            seconds < 60 ->
                "Now"

            seconds < 3600 ->
                "${seconds / 60}m"

            seconds < 86400 ->
                "${seconds / 3600}h"

            seconds < 604800 ->
                "${seconds / 86400}d"

            else ->
                "${seconds / 604800}w"
        }
    } catch (_: Exception) {
        timestamp
    }

private fun formatMemberSince(
    timestamp: String
): String =
    try {
        val instant = Instant.parse(timestamp)
        val date =
            java.time.ZonedDateTime.ofInstant(
                instant,
                java.time.ZoneId.systemDefault()
            )

        date.format(
            java.time.format.DateTimeFormatter
                .ofPattern("MMMM yyyy")
        )
    } catch (e: Exception) {
        ""
    }

private fun ProfileRow.toStudent(
    sessionEmail: String = ""
): Student =
    Student(
        id = id,
        name = name,
        avatarUrl =
            avatarUrl
                ?: "https://i.pravatar.cc/150?u=$id",
        college = college ?: "",
        course = course ?: "",
        year = year ?: "",
        verification =
            VerificationState(
                emailVerified,
                studentVerified,
                phoneVerified
            ),
        rating = rating,
        username = username ?: "",
        email = sessionEmail,
        phone = mobileNumber ?: "",
        location = location ?: "",
        bio = bio ?: "",
        memberSince =
            createdAt?.let {
                formatMemberSince(it)
            } ?: ""
    )

private fun ListingRow.toListing(
    seller: Student
): Listing =
    Listing(
        id = id,
        title = title,
        price = price,
        category =
            ListingCategory.entries.find {
                it.name == category
            } ?: ListingCategory.OTHER,
        subCategory = subCategory ?: "",
        condition =
            ListingCondition.entries.find {
                it.name == condition
            } ?: ListingCondition.GOOD,
        distanceKm = distanceKm,
        imageRes = 0,
        imageUrl = imageUrl,
        description = description ?: "",
        postedDaysAgo = 0,
        seller = seller,
        latitude = latitude,
        longitude = longitude,
        address = address,
        createdAt = createdAt
    )

private fun RoomRow.toRoomListing(
    owner: Student
): RoomListing =
    RoomListing(
        id = id,
        title = title,
        rentPerMonth = rentPerMonth,
        type =
            RoomType.entries.find {
                it.name == type
            } ?: RoomType.SINGLE,
        furnishing =
            FurnishState.entries.find {
                it.name == furnishing
            } ?: FurnishState.UNFURNISHED,
        foodIncluded = foodIncluded,
        distanceKm = distanceKm,
        imageRes = 0,
        imageUrl = imageUrl,
        description = description ?: "",
        postedDaysAgo = 0,
        owner = owner,
        latitude = latitude,
        longitude = longitude,
        address = address,
        createdAt = createdAt
    )

