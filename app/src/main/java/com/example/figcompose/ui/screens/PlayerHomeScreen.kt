package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.ui.theme.TextPrimary
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHomeScreen(
    playerId: Int,
    playerName: String,
    onBack: () -> Unit = {},
    onSubmitFeedback: () -> Unit = {},
    onSettings: () -> Unit = {},
    onOpenMetrics: () -> Unit = {}
) {
    // Reuse ProfileScreen UI, but show only Submit Feedback and hide Edit
    ProfileScreen(
        playerId = playerId,
        playerName = playerName,
        onBack = onBack,
        onEdit = {},
        onSubmitFeedback = onSubmitFeedback,
        showEdit = false,
        showSubmitFeedback = true,
        showBack = false,
        leftIcon = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Person, contentDescription = "Settings")
            }
        },
        topBarActions ={
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { onOpenMetrics() }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Metrics", tint = Color.White)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlayerHomeScreenPreview() {
    FigcomposeTheme {
        PlayerHomeScreen(playerId = 1, playerName = "Player One")
    }
}
