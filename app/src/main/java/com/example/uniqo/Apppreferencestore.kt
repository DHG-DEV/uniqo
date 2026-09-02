package com.example.uniqo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Phase 3 — App Preferences: Notifications, Location Preferences, Language.
 * Separate DataStore from ThemePreferences.kt's uniqo_theme_prefs, so this
 * addition never touches that existing file.
 */
private val Context.appPrefsDataStore by preferencesDataStore(name = "uniqo_app_prefs")

data class NotificationSettings(
    val messages: Boolean = true,
    val roommateMatches: Boolean = true,
    val marketplaceActivity: Boolean = true,
    val offersRequests: Boolean = true,
    val generalUpdates: Boolean = true
)

object NotificationPreferences {
    private val MESSAGES = booleanPreferencesKey("notif_messages")
    private val ROOMMATE_MATCHES = booleanPreferencesKey("notif_roommate_matches")
    private val MARKETPLACE = booleanPreferencesKey("notif_marketplace")
    private val OFFERS = booleanPreferencesKey("notif_offers")
    private val GENERAL = booleanPreferencesKey("notif_general")

    fun settings(context: Context): Flow<NotificationSettings> =
        context.appPrefsDataStore.data.map { prefs ->
            NotificationSettings(
                messages = prefs[MESSAGES] ?: true,
                roommateMatches = prefs[ROOMMATE_MATCHES] ?: true,
                marketplaceActivity = prefs[MARKETPLACE] ?: true,
                offersRequests = prefs[OFFERS] ?: true,
                generalUpdates = prefs[GENERAL] ?: true
            )
        }

    suspend fun setMessages(context: Context, enabled: Boolean) {
        context.appPrefsDataStore.edit { it[MESSAGES] = enabled }
    }
    suspend fun setRoommateMatches(context: Context, enabled: Boolean) {
        context.appPrefsDataStore.edit { it[ROOMMATE_MATCHES] = enabled }
    }
    suspend fun setMarketplaceActivity(context: Context, enabled: Boolean) {
        context.appPrefsDataStore.edit { it[MARKETPLACE] = enabled }
    }
    suspend fun setOffersRequests(context: Context, enabled: Boolean) {
        context.appPrefsDataStore.edit { it[OFFERS] = enabled }
    }
    suspend fun setGeneralUpdates(context: Context, enabled: Boolean) {
        context.appPrefsDataStore.edit { it[GENERAL] = enabled }
    }
}

object LanguagePreferences {
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    /** Only "en" is meaningfully supported today; structured so more can be added later. */
    fun language(context: Context): Flow<String> =
        context.appPrefsDataStore.data.map { prefs -> prefs[LANGUAGE_KEY] ?: "en" }

    suspend fun setLanguage(context: Context, code: String) {
        context.appPrefsDataStore.edit { it[LANGUAGE_KEY] = code }
    }
}

object LocationPrefsStore {
    private val PREFERRED_AREAS_KEY = stringPreferencesKey("preferred_areas") // "|"-delimited
    private val SEARCH_RADIUS_KEY = intPreferencesKey("search_radius_km")

    fun preferredAreas(context: Context): Flow<List<String>> =
        context.appPrefsDataStore.data.map { prefs ->
            prefs[PREFERRED_AREAS_KEY]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
        }

    suspend fun addPreferredArea(context: Context, area: String) {
        context.appPrefsDataStore.edit { prefs ->
            val current = prefs[PREFERRED_AREAS_KEY]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
            if (area.isNotBlank() && area !in current) {
                prefs[PREFERRED_AREAS_KEY] = (current + area).joinToString("|")
            }
        }
    }

    suspend fun removePreferredArea(context: Context, area: String) {
        context.appPrefsDataStore.edit { prefs ->
            val current = prefs[PREFERRED_AREAS_KEY]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
            prefs[PREFERRED_AREAS_KEY] = (current - area).joinToString("|")
        }
    }

    fun searchRadiusKm(context: Context): Flow<Int> =
        context.appPrefsDataStore.data.map { prefs -> prefs[SEARCH_RADIUS_KEY] ?: 10 }

    suspend fun setSearchRadiusKm(context: Context, km: Int) {
        context.appPrefsDataStore.edit { it[SEARCH_RADIUS_KEY] = km }
    }
}