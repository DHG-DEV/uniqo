package com.example.uniqo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("STARTUP", "onCreate reached setContent ${System.currentTimeMillis()}")
        setContent {
            // Phase 3: reads the full System/Light/Dark preference instead of
            // the old boolean-only key. SYSTEM resolves against the device's
            // current setting via isSystemInDarkTheme().
            val themeMode by ThemePreferences.themeMode(this).collectAsState(initial = ThemeMode.SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            UniqoTheme(darkTheme = isDarkTheme) {
                UniqoNavGraph()
            }
        }
    }
}