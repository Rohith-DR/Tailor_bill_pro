package com.example.tailorbillpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A237E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF534BAE),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFF5722),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFFF8A50),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF80E27E),
    onTertiaryContainer = Color(0xFF000000),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFCD8D8),
    onErrorContainer = Color(0xFF000000),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF534BAE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1A237E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFF8A50),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFFF5722),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF80E27E),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF4CAF50),
    onTertiaryContainer = Color(0xFFFFFFFF),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
)

@Composable
fun TailorBillProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}