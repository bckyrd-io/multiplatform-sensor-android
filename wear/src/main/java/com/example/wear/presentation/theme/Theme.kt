package com.example.wear.presentation.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.material3.ColorScheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun MultiplatformTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val uiMode = context.resources.configuration.uiMode
    val dark = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    // Use the new brand seed blue (#008EC4) same as phone app
    val scheme: ColorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF008EC4),
        isDark = dark,
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
        onError = schemeFixed.onError,
        isLight = !dark
    )

    MaterialTheme(colors = colors, content = content)
}