package com.example.figcompose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.figcompose.R
import com.example.figcompose.ui.theme.BluePrimary
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary
import com.example.figcompose.util.PlayerProfileState
import com.example.figcompose.util.ProfileManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    playerId: Int,
    playerName: String,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onSubmitFeedback: () -> Unit = {},
    showEdit: Boolean = true,
    showSubmitFeedback: Boolean = true,
    showBack: Boolean = true,
    topBarActions: @Composable RowScope.() -> Unit = {},
    leftIcon: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val profileManager = remember { ProfileManager(context) }
    val state by profileManager.state

    LaunchedEffect(playerId) {
        profileManager.loadUser(playerId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Player Profile",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF374151))
                        }
                    } else {
                        leftIcon?.invoke()
                    }
                },
                actions = topBarActions,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
        ) {
            // Top state info
            if (state is PlayerProfileState.Error) {
                val msg = (state as PlayerProfileState.Error).message
                Text(text = msg, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Avatar + Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                val loadedUser = (state as? PlayerProfileState.Loaded)?.user
                val displayName = loadedUser?.full_name ?: playerName
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                val role = loadedUser?.role ?: "player"
                val team = "Titans" // Not provided by backend
                val position = "Forward" // Not provided by backend
                Text(
                    text = "${role.replaceFirstChar { it.uppercase() }} • Team: $team",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Personal Information
            SectionTitle("Personal Information")
            InfoCard {
                InfoRow("Name", (state as? PlayerProfileState.Loaded)?.user?.full_name ?: playerName)
                Divider(color = Color(0xFFE5E7EB))
                InfoRow("Username", (state as? PlayerProfileState.Loaded)?.user?.username ?: "-")
                Divider(color = Color(0xFFE5E7EB))
                InfoRow("Role", (state as? PlayerProfileState.Loaded)?.user?.role ?: "player")
            }

            // Contact Details
            SectionTitle("Contact Details")
            InfoCard {
                InfoRow("Email", (state as? PlayerProfileState.Loaded)?.user?.email ?: "-")
                Divider(color = Color(0xFFE5E7EB))
                InfoRow("Phone", (state as? PlayerProfileState.Loaded)?.user?.phone ?: "-")
            }

            // Team & Group (placeholder until backend supports team)
            SectionTitle("Team & Group")
            InfoCard {
                InfoRow("Assigned Teams", "Titans")
            }

            // Actions
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showEdit) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Player")
                    }
                }
                if (showSubmitFeedback) {
                    val weight = if (showEdit) 1f else 1f
                    OutlinedButton(
                        onClick = onSubmitFeedback,
                        modifier = Modifier.weight(weight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Feedback")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ProfileScreenPreview() {
    FigcomposeTheme {
        ProfileScreen(playerId = 1, playerName = "Ethan Carter")
    }
}
