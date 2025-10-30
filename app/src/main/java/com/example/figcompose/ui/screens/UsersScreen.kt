package com.example.figcompose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight

import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.figcompose.R
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary
import androidx.compose.ui.platform.LocalContext
import com.example.figcompose.util.UsersManager
import com.example.figcompose.util.UsersState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onBack: () -> Unit = {},
    onUserSelected: (Int, String) -> Unit = { _, _ -> }
) {
    var query by remember { mutableStateOf("") }

    val context = LocalContext.current
    val usersManager = remember { UsersManager(context) }
    val state by usersManager.state

    LaunchedEffect(Unit) {
        usersManager.loadUsers(limit = 200)
    }

    val allUsers = (state as? UsersState.Loaded)?.users.orEmpty()
    val filtered = remember(query, allUsers) {
        if (query.isBlank()) allUsers else allUsers.filter {
            val name = it.full_name ?: it.username ?: it.email ?: ""
            name.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Select User",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Back", tint = Color(0xFF374151))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Loading/Error states
            if (state is UsersState.Error) {
                val msg = (state as UsersState.Error).message
                Text(
                    text = msg,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search user", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Text(
                text = "Users",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state is UsersState.Loading && allUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.id }) { user ->
                    val displayName = user.full_name ?: user.username ?: "User #${user.id}"
                    val roleLabel = user.role?.replaceFirstChar { ch -> ch.uppercase() } ?: "User"
                    PlayerRow(player = Player(displayName, roleLabel), onClick = { onUserSelected(user.id, displayName) })
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            // If you add real avatars, replace this with AsyncImage/coil. Using a person icon placeholder.
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
            Text(
                text = player.position,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF9CA3AF)
        )
    }
}

private data class Player(val name: String, val position: String)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun UsersScreenPreview() {
    FigcomposeTheme {
        UsersScreen()
    }
}
