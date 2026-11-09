package com.zuct.ngongolebook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B7F3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F0DD),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = Color(0xFF8A5A00),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7ED68F),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF0F5B28),
    onPrimaryContainer = Color(0xFFD7F0DD),
    secondary = Color(0xFFFFC662),
    error = Color(0xFFFFB4AB)
)

/** Follows the phone's dark-mode setting automatically. */
@Composable
fun NgongoleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
