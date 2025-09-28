package com.example.wear.presentation.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.wear.util.UsersManager
import com.example.wear.service.UserDto

@Composable
fun SelectPlayerScreen(
    onPlayerSelected: (Int, String) -> Unit
) {
    val usersManager = remember { UsersManager() }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            users = usersManager.getPlayers(limit = 200)
        } catch (e: Exception) {
            error = e.message ?: "Failed to load players"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select Player", style = MaterialTheme.typography.caption1)
        Spacer(Modifier.height(6.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text(text = error!!, style = MaterialTheme.typography.caption2)
            }
            else -> {
                ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(users, key = { it.id }) { u ->
                        val name = u.full_name ?: u.username ?: u.email ?: "User #${u.id}"
                        Chip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            label = { Text(name) },
                            onClick = { onPlayerSelected(u.id, name) }
                        )
                    }
                }
            }
        }
    }
}
