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

// Deep saffron on dark — high contrast for bright shop counters
val Saffron = Color(0xFFFF6B00)
val SaffronDim = Color(0xFFB34B00)
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val OnlineGreen = Color(0xFF2ECC71)
val OfflineOrange = Color(0xFFFF9F1C)
val WarnRed = Color(0xFFE74C3C)
val TextWhite = Color(0xFFF5F5F5)

private val KhaataColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    secondary = OfflineOrange,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = WarnRed
)

// Min 18sp everywhere, 24sp bill items — readable for a 60-year-old at a counter
private val KhaataTypography = Typography(
    bodyLarge = TextStyle(fontSize = 20.sp),
    bodyMedium = TextStyle(fontSize = 18.sp),
    titleLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
    labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun KhaataTheme(content: @Composable () -> Unit) {
    isSystemInDarkTheme() // always dark by design; call keeps lint happy
    MaterialTheme(
        colorScheme = KhaataColors,
        typography = KhaataTypography,
        content = content
    )
}
