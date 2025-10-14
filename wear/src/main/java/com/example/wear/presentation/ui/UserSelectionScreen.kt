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
                    
                    items(players) { player ->
                        Chip(
                            onClick = {
                                // Navigate to session selection screen
                                navController.navigate("sessionSelection/${player.id}/${player.fullName ?: player.username}")
                            },
                            label = { Text(text = player.fullName ?: player.username) },
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
