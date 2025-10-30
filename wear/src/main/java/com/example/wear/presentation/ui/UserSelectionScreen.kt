package com.example.wear.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.wear.presentation.model.User
import com.example.wear.presentation.service.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun UserSelectionScreen(navController: NavHostController) {
    // State - React Native style!
    var players by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Query for search
    var query by remember { mutableStateOf("") }

    // Fetch players on screen load
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val allUsers = RetrofitClient.apiService.getUsers()
                // Filter only players
                players = allUsers.filter { it.role == "player" }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load players: ${e.message}"
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
                    Text(
                        text = errorMessage ?: "Error",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                val filteredPlayers = if (query.isBlank()) players else players.filter { u ->
                    val name = (u.fullName ?: u.username) ?: ""
                    name.contains(query, ignoreCase = true)
                }

                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Select Player",
                            style = MaterialTheme.typography.title3,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    item {
                        // Search input: border-only, no fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (query.isBlank()) {
                                Text("Search players", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(filteredPlayers) { player ->
                        val display = player.fullName ?: player.username
                        val initial = display.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
                        Chip(
                            onClick = {
                                // Navigate to session selection screen
                                navController.navigate("sessionSelection/${player.id}/${player.fullName ?: player.username}")
                            },
                            label = { Text(text = display) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colors.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initial, color = MaterialTheme.colors.onSurface)
                                }
                            },
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
