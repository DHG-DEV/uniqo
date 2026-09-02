package com.example.uniqo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors
    @Composable get() = lightColorScheme(
        primary = PurplePrimary,
        onPrimary = CardWhite,
        primaryContainer = PurpleLight,
        onPrimaryContainer = PurpleDeep,
        background = Background,
        onBackground = TextPrimary,
        surface = CardWhite,
        onSurface = TextPrimary,
        surfaceVariant = Background,
        onSurfaceVariant = TextSecondary,
        outline = Divider,
        error = ErrorRed,
    )

// ADD-ON: matching dark scheme — same structure as LightColors, but every
// value below resolves through LocalUniqoColors, which UniqoTheme sets to
// DarkUniqoColors when darkTheme = true.
private val DarkColors
    @Composable get() = darkColorScheme(
        primary = PurplePrimary,
        onPrimary = CardWhite,
        primaryContainer = PurpleLight,
        onPrimaryContainer = PurpleDeep,
        background = Background,
        onBackground = TextPrimary,
        surface = CardWhite,
        onSurface = TextPrimary,
        surfaceVariant = Background,
        onSurfaceVariant = TextSecondary,
        outline = Divider,
        error = ErrorRed,
    )

@Composable
fun UniqoTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    // ADD-ON: this is what makes every existing screen's Background/CardWhite/
    // TextPrimary/etc. actually switch — without providing this, those
    // properties would stay on the light palette forever regardless of
    // which MaterialTheme colorScheme is active below.
    val uniqoPalette = if (darkTheme) DarkUniqoColors else LightUniqoColors

    CompositionLocalProvider(LocalUniqoColors provides uniqoPalette) {
        val colorScheme = if (darkTheme) DarkColors else LightColors
        val view = LocalView.current

        // Status bar behavior unchanged from before — always navy with light
        // icons, same as your original design intent for this element.
        if (!view.isInEditMode) {
            SideEffect {
                val activity = view.context as? android.app.Activity
                val window = activity?.window
                if (window != null) {
                    window.statusBarColor = NavyDark.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}