package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.util.SessionManager
import com.example.figcompose.util.SessionsListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHomeScreen(
    playerId: Int,
    playerName: String,
    onBack: () -> Unit = {},
    onSubmitFeedback: (Int?) -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val listState by sessionManager.listState

    LaunchedEffect(Unit) {
        sessionManager.loadSessions()
    }

    val lastSessionId: Int? = when (val s = listState) {
        is SessionsListState.Loaded -> s.sessions.maxByOrNull { it.start_time ?: "" }?.id
        else -> null
    }

    // Reuse ProfileScreen UI, but show only Submit Feedback and hzide Edit
    ProfileScreen(
        playerId = playerId,
        playerName = playerName,
        onBack = onBack,
        onEdit = {},
        onSubmitFeedback = { onSubmitFeedback(lastSessionId) },
        showEdit = false,
        showSubmitFeedback = true,
        showBack = false,
        leftIcon = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
