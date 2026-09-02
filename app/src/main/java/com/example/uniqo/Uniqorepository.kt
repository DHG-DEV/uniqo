package com.example.uniqo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ProfileEdits(
    val name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val location: String? = null,
    val avatarUrl: String? = null
)

interface UniqoRepository {
    val listings: StateFlow<List<Listing>>
    val roomListings: StateFlow<List<RoomListing>>
    val conversations: StateFlow<List<Conversation>>
    val notifications: StateFlow<List<AppNotification>>
    val myPreferences: StateFlow<RoommatePreferences>
    val favorites: StateFlow<Set<String>>
    val activeFilters: StateFlow<ListingFilters>
    val currentUserProfile: StateFlow<Student>
    val lookingForPreferences: StateFlow<List<LookingForPreference>>

    fun currentUser(): Student
    fun toggleFavorite(listingId: String)
    fun isFavorite(listingId: String): Boolean
    fun addListing(listing: Listing)
    fun sendMessage(conversationId: String, text: String)
    fun sendAttachmentMessage(conversationId: String, bytes: ByteArray, fileName: String, mimeType: String)
    fun updatePreferences(preferences: RoommatePreferences)
    fun roommateCandidates(): List<RoommateCandidate>
    fun markNotificationsRead()
    fun setCategoryFilter(category: ListingCategory?)
    fun updateFilters(filters: ListingFilters)
    fun updateProfile(edits: ProfileEdits)
    fun sendOffer(listing: Listing)
    fun findOrCreateConversation(userId: String, name: String, avatarUrl: String): String
    suspend fun searchUsersByUsername(query: String): List<Student>
    fun markConversationRead(conversationId: String)
    fun togglePinConversation(conversationId: String)
    fun addLookingForPreference(category: LookingForCategory, fields: Map<String, String>)
    fun updateLookingForPreference(id: String, fields: Map<String, String>)
    fun deactivateLookingForPreference(id: String)

    // ADD-ON: fetch any user's profile by id (for seller/owner profile pages,
    // e.g. Routes.userProfile(sellerId)). Default returns null so nothing
    // implementing this interface breaks until it opts in with its own override.
    suspend fun getUserProfile(userId: String): Student? = null
}

class MockRepository : UniqoRepository {
    override val listings = MutableStateFlow(MockData.listings)
    override val roomListings = MutableStateFlow(MockData.roomListings)
    override val conversations = MutableStateFlow(MockData.conversations)
    override val notifications = MutableStateFlow(MockData.notifications)
    override val myPreferences = MutableStateFlow(MockData.myPreferences)
    override val favorites = MutableStateFlow(setOf<String>("l6"))
    override val activeFilters = MutableStateFlow(ListingFilters())
    override val lookingForPreferences = MutableStateFlow<List<LookingForPreference>>(emptyList())

    override val currentUserProfile = MutableStateFlow(
        MockData.currentUser.copy(
            username = MockData.currentUser.name.lowercase().replace(" ", ""),
            email = "you@example.com",
            phone = "+91 90000 00000",
            location = "Delhi, India",
            bio = "Computer Science Student",
            memberSince = "March 2023"
        )
    )

    override fun currentUser() = currentUserProfile.value

    override fun toggleFavorite(listingId: String) {
        favorites.update { current ->
            if (listingId in current) current - listingId else current + listingId
        }
    }

    override fun isFavorite(listingId: String) = listingId in favorites.value

    override fun addListing(listing: Listing) {
        listings.update { listOf(listing) + it }
    }

    override fun sendMessage(conversationId: String, text: String) {
        conversations.update { convos ->
            convos.map { convo ->
                if (convo.id == conversationId) {
                    val msg = ChatMessage(
                        id = "m${System.currentTimeMillis()}",
                        senderId = currentUser().id,
                        text = text,
                        timestamp = "Now",
                        isMine = true
                    )
                    convo.copy(messages = convo.messages + msg, lastMessage = text, time = "Now")
                } else convo
            }
        }
    }

    override fun sendAttachmentMessage(
        conversationId: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ) {}

    override fun updatePreferences(preferences: RoommatePreferences) {
        myPreferences.value = preferences
    }

    override fun roommateCandidates() = MockData.roommateCandidates

    override fun markNotificationsRead() {
        notifications.update { it.map { n -> n.copy(isUnread = false) } }
    }

    override fun setCategoryFilter(category: ListingCategory?) {
        activeFilters.update { it.copy(category = category) }
    }

    override fun updateFilters(filters: ListingFilters) {
        activeFilters.value = filters
    }

    override fun updateProfile(edits: ProfileEdits) {
        currentUserProfile.update { current ->
            current.copy(
                name = edits.name ?: current.name,
                username = edits.username ?: current.username,
                bio = edits.bio ?: current.bio,
                email = edits.email ?: current.email,
                phone = edits.phone ?: current.phone,
                location = edits.location ?: current.location,
                avatarUrl = edits.avatarUrl ?: current.avatarUrl
            )
        }
    }

    override fun sendOffer(listing: Listing) {
        val buyer = currentUser()
        notifications.update {
            listOf(
                AppNotification(
                    id = "n${System.currentTimeMillis()}",
                    actor = buyer.name,
                    avatarUrl = buyer.avatarUrl,
                    message = "${buyer.name} wants to buy your listing \"${listing.title}\"",
                    time = "Now",
                    type = NotificationType.OFFER,
                    isUnread = true,
                    category = listing.category.label,
                    relatedId = listing.id,
                    actorId = buyer.id
                )
            ) + it
        }
    }

    override fun findOrCreateConversation(
        userId: String,
        name: String,
        avatarUrl: String
    ): String {
        conversations.value.firstOrNull { it.participant.id == userId }?.let { return it.id }

        val student = MockData.allStudents.firstOrNull { it.id == userId }
            ?: Student(userId, name, avatarUrl, "", "", "")

        val convo = Conversation(
            id = "c_$userId",
            participant = student,
            lastMessage = "",
            time = "Now",
            unreadCount = 0,
            context = ChatFilter.MARKETPLACE,
            messages = emptyList()
        )

        conversations.update { listOf(convo) + it }
        return convo.id
    }

    override suspend fun searchUsersByUsername(query: String): List<Student> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        val myId = currentUser().id
        return MockData.allStudents.filter {
            it.id != myId && it.username.isNotBlank() && it.username.contains(q, true)
        }
    }

    override fun markConversationRead(conversationId: String) {
        conversations.update { list ->
            list.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it }
        }
    }

    override fun togglePinConversation(conversationId: String) {
        conversations.update { list ->
            list.map {
                if (it.id == conversationId) it.copy(isPinned = !it.isPinned) else it
            }.sortedByDescending { it.isPinned }
        }
    }

    override fun addLookingForPreference(
        category: LookingForCategory,
        fields: Map<String, String>
    ) {
        val pref = LookingForPreference(
            id = "lf_${System.currentTimeMillis()}",
            category = category,
            isActive = true,
            fields = fields
        )
        lookingForPreferences.update { listOf(pref) + it }
    }

    override fun updateLookingForPreference(
        id: String,
        fields: Map<String, String>
    ) {
        lookingForPreferences.update { list ->
            list.map { if (it.id == id) it.copy(fields = fields) else it }
        }
    }

    override fun deactivateLookingForPreference(id: String) {
        lookingForPreferences.update { list ->
            list.filterNot { it.id == id }
        }
    }

    // ADD-ON: mock lookup for any user's profile by id.
    override suspend fun getUserProfile(userId: String): Student? =
        MockData.allStudents.firstOrNull { it.id == userId }
}

object RepositoryProvider {
    val repository: UniqoRepository by lazy { SupabaseRepository() }
}
