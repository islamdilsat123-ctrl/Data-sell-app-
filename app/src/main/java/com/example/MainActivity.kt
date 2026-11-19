package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.ThemePreferences
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.DilsatDataTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePrefs = remember { ThemePreferences(applicationContext) }
            val themeMode by themePrefs.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.DARK)
            val scope = rememberCoroutineScope()
            val systemDark = isSystemInDarkTheme()
            val useDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }

            DilsatDataTheme(darkTheme = useDark) {
                AppNavigation(
                    themeMode = themeMode,
                    isDarkTheme = useDark,
                    onSelectTheme = { mode ->
                        scope.launch { themePrefs.setThemeMode(mode) }
                    }
                )
            }
        }
    }
}
