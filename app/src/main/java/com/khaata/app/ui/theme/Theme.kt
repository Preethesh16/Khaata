package com.khaata.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Khaata palette — deep saffron on dark, high contrast for sunlight at a shop counter
val Saffron = Color(0xFFFF6B00)
val SaffronLight = Color(0xFFFF8F3C)
val DarkBg = Color(0xFF121212)
val CardBg = Color(0xFF1E1E1E)
val OnlineGreen = Color(0xFF2ECC71)
val OfflineOrange = Color(0xFFFFA726)
val WarnRed = Color(0xFFE74C3C)
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB0B0B0)

private val KhaataColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    secondary = SaffronLight,
    background = DarkBg,
    surface = CardBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = WarnRed
)

// Rule: nothing under 18sp; bill items 24sp
private val KhaataTypography = Typography(
    bodyLarge = TextStyle(fontSize = 20.sp, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 18.sp, color = TextPrimary),
    titleLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    titleMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
)

@Composable
fun KhaataTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KhaataColors,
        typography = KhaataTypography,
        content = content
    )
}
