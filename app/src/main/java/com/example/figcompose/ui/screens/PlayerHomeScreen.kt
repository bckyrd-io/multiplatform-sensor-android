package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import com.example.figcompose.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHomeScreen(
    playerId: Int,
    playerName: String,
    onBack: () -> Unit = {},
    onSubmitFeedback: (Int?) -> Unit = {},
    onSettings: () -> Unit = {},
    onOpenMetrics: (Int, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val listState by sessionManager.listState

    LaunchedEffect(Unit) {
        sessionManager.loadSessions()
    }

    val lastSession = when (val s = listState) {
        is SessionsListState.Loaded -> s.sessions.maxByOrNull { it.start_time ?: "" }
        else -> null
    }
    val lastSessionId: Int? = lastSession?.id
    val lastSessionTitle: String = lastSession?.title ?: ""

    // Reuse ProfileScreen UI, but show only Submit Feedback and hzide Edit
    ProfileScreen(
        playerId = playerId,
        playerName = playerName,
        onBack = onBack,
        onEdit = {},
        onSubmitFeedback = {
            if (lastSessionId == null) {
                Toast.makeText(context, "Sessions are not created", Toast.LENGTH_SHORT).show()
            } else {
                onSubmitFeedback(lastSessionId)
            }
        },
        showEdit = false,
        showSubmitFeedback = true,
        showBack = false,
        leftIcon = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        },
        topBarActions ={
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BluePrimary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = {
                    if (lastSessionId == null) {
                        Toast.makeText(context, "Sessions are not created", Toast.LENGTH_SHORT).show()
                    } else {
                        onOpenMetrics(lastSessionId, lastSessionTitle.ifBlank { "Session #$lastSessionId" })
                    }
                }) {
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
