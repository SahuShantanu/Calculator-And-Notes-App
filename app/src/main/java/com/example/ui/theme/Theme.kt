package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    secondary = SleekDarkSecondary,
    tertiary = SleekDarkTertiary,
    background = SleekDarkBg,
    surface = SleekDarkSurface,
    surfaceVariant = SleekDarkCard,
    onPrimary = SleekDarkBg,
    onSecondary = SleekDarkBg,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF9DB2AE)
)

private val LightColorScheme = lightColorScheme(
    primary = SleekLightPrimary,
    secondary = SleekLightSecondary,
    tertiary = SleekLightTertiary,
    background = SleekLightBg,
    surface = SleekLightSurface,
    surfaceVariant = SleekLightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1B2021),
    onSurface = Color(0xFF1B2021),
    onSurfaceVariant = Color(0xFF5F6368)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
