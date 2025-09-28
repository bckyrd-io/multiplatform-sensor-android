package com.example.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

// Match app palette
private val BluePrimary = Color(0xFF0A84FF)
private val BluePrimaryDark = Color(0xFF0669CC)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF6B7280)

private val WearColors = Colors(
    primary = BluePrimary,
    primaryVariant = BluePrimaryDark,
    secondary = BluePrimary,
    secondaryVariant = BluePrimaryDark,
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFFF3B30),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = Color.White
)

@Composable
fun FigcomposeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColors,
        content = content
    )
}