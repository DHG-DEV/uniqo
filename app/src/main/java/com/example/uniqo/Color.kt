package com.example.uniqo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/* ============================================================================
 * DARK THEME SUPPORT — every color name below that used to be a plain
 * top-level `val` is now a @Composable computed property reading from
 * whichever UniqoColors palette is currently provided (see UniqoTheme.kt,
 * which sets this once at the app root based on the persisted preference).
 *
 * Nothing else needs to change: every existing screen already references
 * these exact same names (Background, CardWhite, TextPrimary, ...) from
 * inside @Composable functions, so the switch is invisible to them.
 * ========================================================================== */

data class UniqoColors(
    val background: Color,
    val cardWhite: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val shadow: Color,
    val bottomNavBackground: Color,
    val tabUnselected: Color,
    val pastelBlue: Color,
    val pastelGreen: Color,
    val pastelPeach: Color,
    val pastelLavender: Color,
    val pastelPink: Color
)

val LightUniqoColors = UniqoColors(
    background = Color(0xFFF8F7FC),
    cardWhite = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF171B32),
    textSecondary = Color(0xFF7B8195),
    divider = Color(0xFFE5E3EC),
    shadow = Color(0x14000000),
    bottomNavBackground = Color(0xFFF0EEFF),
    tabUnselected = Color(0xFFA39DC4),
    pastelBlue = Color(0xFFDCEBFF),
    pastelGreen = Color(0xFFDBF6E5),
    pastelPeach = Color(0xFFFFE8D6),
    pastelLavender = Color(0xFFEDE3FF),
    pastelPink = Color(0xFFFFE1EC)
)

val DarkUniqoColors = UniqoColors(
    background = Color(0xFF0B0E1A),
    cardWhite = Color(0xFF161B2E),
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFF9098B5),
    divider = Color(0xFF262C42),
    shadow = Color(0x33000000),
    bottomNavBackground = Color(0xFF12162A),
    tabUnselected = Color(0xFF6B7290),
    pastelBlue = Color(0xFF1E2A47),
    pastelGreen = Color(0xFF16302A),
    pastelPeach = Color(0xFF3A2A1E),
    pastelLavender = Color(0xFF2A2247),
    pastelPink = Color(0xFF3A2030)
)

val LocalUniqoColors = staticCompositionLocalOf { LightUniqoColors }

// Brand — deliberately constant across both themes; the purple accent
// shouldn't shift when switching modes.
val NavyDark = Color(0xFF0E1433)
val NavyDark2 = Color(0xFF171F45)
val PurplePrimary = Color(0xFF6C5CE7)
val PurpleLight = Color(0xFFEDEAFB)
val PurpleDeep = Color(0xFF5849C4)

// Theme-aware — same names every screen already uses.
val Background: Color
    @Composable get() = LocalUniqoColors.current.background

val CardWhite: Color
    @Composable get() = LocalUniqoColors.current.cardWhite

val TextPrimary: Color
    @Composable get() = LocalUniqoColors.current.textPrimary

val TextSecondary: Color
    @Composable get() = LocalUniqoColors.current.textSecondary

val Divider: Color
    @Composable get() = LocalUniqoColors.current.divider

val Shadow: Color
    @Composable get() = LocalUniqoColors.current.shadow

val BottomNavBackground: Color
    @Composable get() = LocalUniqoColors.current.bottomNavBackground

val TabUnselected: Color
    @Composable get() = LocalUniqoColors.current.tabUnselected

val PastelBlue: Color
    @Composable get() = LocalUniqoColors.current.pastelBlue

val PastelGreen: Color
    @Composable get() = LocalUniqoColors.current.pastelGreen

val PastelPeach: Color
    @Composable get() = LocalUniqoColors.current.pastelPeach

val PastelLavender: Color
    @Composable get() = LocalUniqoColors.current.pastelLavender

val PastelPink: Color
    @Composable get() = LocalUniqoColors.current.pastelPink

// Category icon tints — kept vivid/constant across both themes; they read
// fine on both light and dark backgrounds per the reference.
val IconBlue = Color(0xFF3E7BFA)
val IconGreen = Color(0xFF2AAE64)
val IconOrange = Color(0xFFFF8A3D)
val IconPurple = Color(0xFF7C5CFF)
val IconPink = Color(0xFFFF5C8A)

// Semantic — kept constant across both themes so they stay recognizable
// (a green success or red error shouldn't change meaning between modes).
val SuccessGreen = Color(0xFF2EAD68)
val WarningAmber = Color(0xFFFFAA00)
val ErrorRed = Color(0xFFE8544F)
val StarGold = Color(0xFFFFAA00)
val Favorite = Color(0xFFFF5C7A)
val ProfileBlue = Color(0xFF1769D5)