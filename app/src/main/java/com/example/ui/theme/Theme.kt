package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class DilsatExtendedColors(
    val neonGreen: Color,
    val electricCyan: Color,
    val vividPurple: Color,
    val cardElevated: Color,
    val borderSubtle: Color,
    val statusSuccess: Color,
    val statusPending: Color,
    val statusProcessing: Color,
    val statusError: Color,
    val isDark: Boolean
)

val LocalDilsatColors = staticCompositionLocalOf {
    DilsatExtendedColors(
        neonGreen = NeonGreen,
        electricCyan = ElectricCyan,
        vividPurple = VividPurple,
        cardElevated = ElevatedCard,
        borderSubtle = DarkOutline,
        statusSuccess = StatusSuccess,
        statusPending = StatusPending,
        statusProcessing = StatusProcessing,
        statusError = StatusError,
        isDark = true
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = ObsidianBg,
    primaryContainer = Color(0xFF063A2B),
    onPrimaryContainer = NeonGreen,
    secondary = ElectricCyan,
    onSecondary = ObsidianBg,
    secondaryContainer = Color(0xFF072D3D),
    onSecondaryContainer = ElectricCyan,
    tertiary = VividPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF28194E),
    onTertiaryContainer = SoftLavender,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = MidnightNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkOutline,
    error = StatusError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = DeepCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = RoyalPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF4C1D95),
    background = IceWhiteBg,
    onBackground = TextPrimaryLight,
    surface = PureWhiteSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SoftSlateCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutline,
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DilsatShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DilsatDataTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        DilsatExtendedColors(
            neonGreen = NeonGreen,
            electricCyan = ElectricCyan,
            vividPurple = VividPurple,
            cardElevated = ElevatedCard,
            borderSubtle = DarkOutline,
            statusSuccess = StatusSuccess,
            statusPending = StatusPending,
            statusProcessing = StatusProcessing,
            statusError = StatusError,
            isDark = true
        )
    } else {
        DilsatExtendedColors(
            neonGreen = EmeraldPrimary,
            electricCyan = DeepCyan,
            vividPurple = RoyalPurple,
            cardElevated = PureWhiteSurface,
            borderSubtle = LightOutline,
            statusSuccess = Color(0xFF059669),
            statusPending = Color(0xFFD97706),
            statusProcessing = Color(0xFF0284C7),
            statusError = Color(0xFFDC2626),
            isDark = false
        )
    }

    CompositionLocalProvider(LocalDilsatColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DilsatTypography,
            shapes = DilsatShapes,
            content = content
        )
    }
}
