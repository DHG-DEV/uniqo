package com.example.uniqo

data class VerificationState(
    val emailVerified: Boolean = false,
    val studentVerified: Boolean = false,
    val phoneVerified: Boolean = false
) {
    val isVerified get() = emailVerified && studentVerified
}

data class Student(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val college: String,
    val course: String,
    val year: String,
    val verification: VerificationState = VerificationState(true, true, false),
    val rating: Double = 4.8,
    val listingsCount: Int = 0,
    val soldCount: Int = 0,
    val lastActiveAt: Long? = null,
    // Added for the Profile screen. All default to "" so every existing
    // place that constructs a Student (mock data, listing/room sellers,
    // roommate candidates) keeps compiling unchanged.
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val bio: String = "",
    val memberSince: String = "" // display-ready, e.g. "March 2023"
)

enum class ListingCategory(val label: String) {
    FURNITURE("Furniture"), ELECTRONICS("Electronics"), BOOKS("Books"),
    APPLIANCES("Appliances"), ROOMS("Rooms"), ROOMMATE("Roommate"), OTHER("Other")
}

enum class ListingCondition(val label: String) {
    NEW("New"), LIKE_NEW("Like New"), GOOD("Good"), USED("Used")
}

data class Listing(
    val id: String, val title: String, val price: Int,
    val category: ListingCategory, val subCategory: String,
    val condition: ListingCondition, val distanceKm: Double,
    val imageRes: Int, val imageUrl: String? = null,
    val description: String, val postedDaysAgo: Int, val seller: Student,
    var isFavorite: Boolean = false, val latitude: Double? = null,
    val longitude: Double? = null, val address: String? = null,
    val createdAt: String? = null
)

enum class RoomType(val label: String) {
    SINGLE("Single Room"), SHARED("Shared Room"), PG("PG"), FLAT("Flat")
}

enum class FurnishState(val label: String) {
    FURNISHED("Furnished"), SEMI_FURNISHED("Semi Furnished"), UNFURNISHED("Unfurnished")
}

data class RoomListing(
    val id: String, val title: String, val rentPerMonth: Int,
    val type: RoomType, val furnishing: FurnishState,
    val foodIncluded: Boolean = false, val distanceKm: Double,
    val imageRes: Int, val imageUrl: String? = null,
    val description: String, val postedDaysAgo: Int, val owner: Student,
    val latitude: Double? = null, val longitude: Double? = null,
    val address: String? = null, val createdAt: String? = null
)

enum class SleepSchedule(val label: String) {
    EARLY_BIRD("Early Bird"), FLEXIBLE("Flexible"), LATE("Late")
}

enum class YesNo(val label: String) { YES("Yes"), NO("No") }
enum class FoodPref(val label: String) { VEG("Veg"), NON_VEG("Non-Veg"), EITHER("Either") }
enum class CleanlinessLevel(val label: String) { LOW("Low"), MEDIUM("Medium"), HIGH("High") }
enum class StudyEnvironment(val label: String) { QUIET("Quiet"), MODERATE("Moderate"), SOCIAL("Social") }

data class RoommatePreferences(
    val budgetMin: Int = 5000, val budgetMax: Int = 7000,
    val sleepSchedule: SleepSchedule = SleepSchedule.LATE,
    val smoking: YesNo = YesNo.NO, val food: FoodPref = FoodPref.VEG,
    val cleanliness: CleanlinessLevel = CleanlinessLevel.HIGH,
    val pets: YesNo = YesNo.NO,
    val studyEnvironment: StudyEnvironment = StudyEnvironment.QUIET,
    val maxDistanceKm: Double = 5.0
)

data class RoommateCandidate(val student: Student, val preferences: RoommatePreferences, val distanceKm: Double)

data class RoommateMatch(
    val candidate: RoommateCandidate,
    val scorePercent: Int,
    val breakdown: Map<String, Int>
)

enum class ChatFilter { ALL, UNREAD, ROOMS, MARKETPLACE }

data class ChatMessage(
    val id: String, val senderId: String, val text: String, val timestamp: String,
    val isMine: Boolean, val isRead: Boolean = true,
    val messageType: String = "text", val fileUrl: String? = null,
    val fileName: String? = null, val fileMimeType: String? = null,
    val fileSize: Long? = null
)

data class Conversation(
    val id: String, val participant: Student, val lastMessage: String,
    val time: String, val unreadCount: Int = 0,
    val context: ChatFilter = ChatFilter.MARKETPLACE,
    val messages: List<ChatMessage> = emptyList(),
    val isPinned: Boolean = false
)

enum class NotificationType { MESSAGE, VIEW, LIKE, OFFER, REPLY, ANNOUNCEMENT, NEW_LISTING }

data class AppNotification(
    val id: String, val actor: String, val avatarUrl: String,
    val message: String, val time: String, val type: NotificationType,
    val isUnread: Boolean = true, val category: String? = null,
    val relatedId: String? = null, val rating: Double? = null, val actorId: String? = null
)

enum class ReportReason(val label: String) {
    SCAM("Scam"), FAKE_LISTING("Fake listing"), INAPPROPRIATE("Inappropriate content"),
    HARASSMENT("Harassment"), SPAM("Spam"), OTHER("Other")
}

enum class SortOption(val label: String) {
    NEWEST("Newest First"), PRICE_LOW_HIGH("Price Low to High"),
    PRICE_HIGH_LOW("Price High to Low"), NEAREST("Nearest")
}

data class ListingFilters(
    val category: ListingCategory? = null, val minPrice: Int = 0,
    val maxPrice: Int = 20000, val maxDistanceKm: Double = 10.0,
    val condition: ListingCondition? = null, val sortBy: SortOption = SortOption.NEWEST
)
