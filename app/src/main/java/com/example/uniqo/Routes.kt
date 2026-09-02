package com.example.uniqo

object Routes {

    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    // ---------------------------------------------------------
    // MAIN
    // ---------------------------------------------------------

    const val HOME = "home"
    const val MARKET = "market"
    const val ROOMS = "rooms"
    const val CHAT_LIST = "chat_list"
    const val PROFILE = "profile"

    // ---------------------------------------------------------
    // LOOKING FOR
    // ---------------------------------------------------------

    const val LOOKING_FOR = "looking_for"

    const val LOOKING_FOR_CATEGORY_SELECT =
        "looking_for_category_select"

    const val LOOKING_FOR_FORM =
        "looking_for_form/{category}?prefId={prefId}"

    fun lookingForForm(
        category: LookingForCategory,
        prefId: String? = null
    ) = "looking_for_form/${category.name}?prefId=${prefId ?: ""}"

    // ---------------------------------------------------------
    // LISTINGS
    // ---------------------------------------------------------

    const val LISTING_DETAIL = "listing/{listingId}"

    fun listingDetail(id: String) =
        "listing/$id"

    const val POST_LISTING = "post_listing"

    // ---------------------------------------------------------
    // ROOMS
    // ---------------------------------------------------------

    const val ROOM_DETAIL = "room/{roomId}"

    fun roomDetail(id: String) =
        "room/$id"

    // ---------------------------------------------------------
    // ROOMMATE
    // ---------------------------------------------------------

    const val ROOMMATE_MATCH = "roommate_match"

    const val ROOMMATE_MATCHES = "roommate_matches"

    const val ROOMMATE_PREFERENCES = "roommate_preferences"

    const val ROOMMATE_PROFILE =
        "roommate_profile/{candidateId}"

    fun roommateProfile(id: String) =
        "roommate_profile/$id"

    const val SAVED_ROOMMATE_PROFILES =
        "saved_roommate_profiles"

    const val ROOMMATE_REQUESTS =
        "roommate_requests"

    const val ROOMMATE_MATCH_HISTORY =
        "roommate_match_history"

    // ---------------------------------------------------------
    // CHAT
    // ---------------------------------------------------------

    const val CHAT_DETAIL =
        "chat/{conversationId}"

    fun chatDetail(id: String) =
        "chat/$id"

    // ---------------------------------------------------------
    // USER PROFILE
    // ---------------------------------------------------------

    const val USER_PROFILE =
        "user_profile/{userId}"

    fun userProfile(id: String) =
        "user_profile/$id"

    // ---------------------------------------------------------
    // MARKETPLACE / GENERAL
    // ---------------------------------------------------------

    const val FILTERS = "filters"
    const val MAP = "map"

    const val NOTIFICATIONS =
        "notifications"

    const val BOOKMARKS =
        "bookmarks"

    const val MY_LISTINGS =
        "my_listings"

    const val MY_OFFERS =
        "my_offers"

    const val PURCHASE_HISTORY =
        "purchase_history"

    const val SELLING_HISTORY =
        "selling_history"

    const val RECENTLY_VIEWED =
        "recently_viewed"

    // ---------------------------------------------------------
    // PROFILE
    // ---------------------------------------------------------

    const val EDIT_PROFILE =
        "edit_profile"

    const val PROFILE_STATS =
        "profile_stats"

    const val ACHIEVEMENTS =
        "achievements"

    // ---------------------------------------------------------
    // ACTIVITY
    // ---------------------------------------------------------

    const val REVIEWS_RECEIVED =
        "reviews_received"

    const val BLOCKED_USERS =
        "blocked_users"

    const val INVITE_FRIENDS =
        "invite_friends"

    // ---------------------------------------------------------
    // ACCOUNT
    // ---------------------------------------------------------

    const val SETTINGS =
        "settings"

    const val NOTIFICATION_SETTINGS =
        "notification_settings"

    const val LOCATION_PREFERENCES =
        "location_preferences"

    const val LANGUAGE =
        "language"

    const val APPEARANCE =
        "appearance"

    const val ACCOUNT_INFO =
        "account_info"

    const val EMAIL_PHONE =
        "email_phone"

    const val CHANGE_PASSWORD =
        "change_password"

    const val DELETE_ACCOUNT =
        "delete_account"

    const val PRIVACY_SAFETY =
        "privacy_safety"

    const val HELP_SUPPORT =
        "help_support"

    const val SAFETY_CENTER =
        "safety_center"

    const val COMMUNITY_GUIDELINES =
        "community_guidelines"

    const val PRIVACY_POLICY =
        "privacy_policy"

    const val TERMS_CONDITIONS =
        "terms_conditions"

    const val ABOUT_UNIQO =
        "about_uniqo"
}