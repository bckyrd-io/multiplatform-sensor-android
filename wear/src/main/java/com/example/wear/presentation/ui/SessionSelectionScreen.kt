package com.example.wear.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.wear.presentation.model.Session
import com.example.wear.presentation.service.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun SessionSelectionScreen(
    navController: NavHostController,
    playerId: Int,
    playerName: String
) {
    // State - React Native style!
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Fetch sessions on screen load
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                sessions = RetrofitClient.apiService.getSessions()
                
                // If sessions exist, auto-select the last one
                if (sessions.isNotEmpty()) {
                    val lastSession = sessions.last()
                    // Auto-navigate to metrics with the last session
                    navController.navigate("userMetrics/$playerId/$playerName/${lastSession.id}/${lastSession.title}") {
                        popUpTo("sessionSelection/$playerId/$playerName") { inclusive = true }
                    }
                } else {
                    isLoading = false
                    errorMessage = "No sessions available"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load sessions: ${e.message}"
                isLoading = false
            }
        }
    }
    
    Scaffold(
        timeText = { TimeText() },
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Error",
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
            else -> {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Select Session",
                            style = MaterialTheme.typography.title3,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    item {
                        Text(
                            text = playerName,
                            style = MaterialTheme.typography.caption1,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    items(sessions) { session ->
                        Chip(
                            onClick = {
                                navController.navigate("userMetrics/$playerId/$playerName/${session.id}/${session.title}")
                            },
                            label = { Text(text = session.title) },
                            secondaryLabel = session.sessionType?.let { { Text(text = it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
