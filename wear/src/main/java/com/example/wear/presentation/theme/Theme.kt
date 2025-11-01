package com.example.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.material3.ColorScheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun MultiplatformTheme(
    content: @Composable () -> Unit
) {
    // Use the new brand seed blue (#008EC4) same as phone app
    val scheme: ColorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF008EC4),
        isDark = false,
        style = PaletteStyle.Expressive
    )
    val schemeFixed = scheme.copy(
        primary = Color(0xFF008EC4),
        secondary = Color(0xFF022658)
    )
    val colors = Colors(
        primary = schemeFixed.primary,
        primaryVariant = schemeFixed.primaryContainer,
        secondary = schemeFixed.secondary,
        secondaryVariant = schemeFixed.secondaryContainer,
        background = schemeFixed.background,
        surface = schemeFixed.surface,
        error = schemeFixed.error,
        onPrimary = schemeFixed.onPrimary,
        onSecondary = schemeFixed.onSecondary,
        onBackground = schemeFixed.onBackground,
        onSurface = schemeFixed.onSurface,
        onError = schemeFixed.onError
    )

    MaterialTheme(colors = colors, content = content)
}