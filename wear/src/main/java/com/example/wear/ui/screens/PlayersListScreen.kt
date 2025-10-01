package com.example.wear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.example.wear.util.UsersManager
import com.example.wear.util.UsersState

@Composable
fun PlayersListScreen(
    onPlayerSelected: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val usersManager = remember { UsersManager(context) }
    val state by usersManager.state

    LaunchedEffect(Unit) {
        usersManager.loadUsers(limit = 200)
    }

    val allUsers = (state as? UsersState.Loaded)?.users.orEmpty()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Select player",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when (state) {
                is UsersState.Loading -> {
                    item {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UsersState.Error -> {
                    val msg = (state as UsersState.Error).message
                    item { Text(text = msg, maxLines = 3, overflow = TextOverflow.Ellipsis) }
                }
                else -> {
                    // no-op
                }
            }

            if (allUsers.isNotEmpty()) {
                items(allUsers, key = { it.id }) { user ->
                    val displayName = user.full_name ?: user.username ?: "User #${user.id}"
                    val roleLabel = user.role?.replaceFirstChar { ch -> ch.uppercase() } ?: "Player"
                    Chip(
                        onClick = { onPlayerSelected(user.id, displayName) },
                        label = {
                            Text(
                                text = displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        secondaryLabel = {
                            Text(text = roleLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }
        }
    }
}
