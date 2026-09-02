package com.example.uniqo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the user's theme choice across app restarts.
 *
 * IMPORTANT — requires this dependency in app/build.gradle.kts if not
 * already present:
 *   implementation("androidx.datastore:datastore-preferences:1.1.1")
 * (version number can be newer; any recent 1.x release works)
 */
private val Context.themeDataStore by preferencesDataStore(name = "uniqo_theme_prefs")

/**
 * ADD-ON (Phase 3 — Appearance): full System / Light / Dark support.
 * Stored separately from the old boolean key below so existing data isn't
 * touched; defaults to SYSTEM for anyone who hasn't chosen yet, including
 * users who only ever used the old on/off toggle.
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromStored(value: String?): ThemeMode =
            entries.find { it.name == value } ?: SYSTEM
    }
}

object ThemePreferences {

    // Legacy key — kept as-is, untouched, for back-compat. No longer read
    // by any screen after the Phase 3 Appearance screen replaced the old
    // inline Settings toggle, but left in place so nothing that might still
    // reference it breaks.
    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")

    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    /** New Appearance API — System / Light / Dark. Defaults to SYSTEM. */
    fun themeMode(context: Context): Flow<ThemeMode> =
        context.themeDataStore.data.map { prefs ->
            ThemeMode.fromStored(prefs[THEME_MODE_KEY])
        }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.themeDataStore.edit { prefs -> prefs[THEME_MODE_KEY] = mode.name }
    }

    // ---- Legacy boolean API — unchanged, kept for back-compat only ----

    /** Emits the current preference immediately and on every change. Defaults to light (false). */
    fun isDarkTheme(context: Context): Flow<Boolean> =
        context.themeDataStore.data.map { prefs -> prefs[DARK_THEME_KEY] ?: false }

    suspend fun setDarkTheme(context: Context, enabled: Boolean) {
        context.themeDataStore.edit { prefs -> prefs[DARK_THEME_KEY] = enabled }
    }
}