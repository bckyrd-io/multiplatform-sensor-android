package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.figcompose.ui.theme.BluePrimary
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary
import com.example.figcompose.util.AuthManager
import com.example.figcompose.util.PlayerProfileState
import com.example.figcompose.util.ProfileManager
import com.example.figcompose.util.UpdateProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    playerId: Int,
    onBack: () -> Unit = {},
    onSaved: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val profileManager = remember { ProfileManager(context) }
    val authManager = remember { AuthManager(context) }
    val state by profileManager.state
    val updateState by profileManager.updateState

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("player") }
    // Admin password (coach can set/reset a user's password directly)
    var adminPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authManager.checkAuthState()
    }

    LaunchedEffect(playerId) {
        profileManager.loadUser(playerId) { success, _ ->
            val u = (profileManager.state.value as? PlayerProfileState.Loaded)?.user
            if (success && u != null) {
                fullName = u.full_name ?: ""
                username = u.username ?: ""
                email = u.email ?: ""
                phone = u.phone ?: ""
                role = u.role ?: "player"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Edit Player",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state is PlayerProfileState.Error) {
                val msg = (state as PlayerProfileState.Error).message
                Text(text = msg, color = TextSecondary)
            }

            LabeledField(label = "Full Name", value = fullName, onChange = { fullName = it })
            LabeledField(label = "Username", value = username, onChange = { username = it })
            LabeledField(label = "Email", value = email, onChange = { email = it })
            LabeledField(label = "Phone", value = phone, onChange = { phone = it })

            // Role selection (coaches only) using radio buttons
            val isCoach = (authManager.currentUser.value?.get("role") as? String) == "coach"
            if (isCoach) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Role",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = role == "player", onClick = { role = "player" })
                            Spacer(Modifier.width(4.dp))
                            Text("Player")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = role == "coach", onClick = { role = "coach" })
                            Spacer(Modifier.width(4.dp))
                            Text("Coach")
                        }
                    }
                }
                // Visible password field for admin (coach)
                LabeledField(label = "Password", value = adminPassword, onChange = { adminPassword = it })
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val isCoachLocal = (authManager.currentUser.value?.get("role") as? String) == "coach"
                    val newPwdToSend = if (adminPassword.isNotBlank()) adminPassword else null
                    val roleToSend = if (isCoachLocal) role else null

                    profileManager.updateUser(
                        userId = playerId,
                        username = username.ifBlank { null },
                        email = email.ifBlank { null },
                        fullName = fullName.ifBlank { null },
                        phone = phone.ifBlank { null },
                        role = roleToSend,
                        currentPassword = null,
                        newPassword = newPwdToSend
                    ) { success, _ ->
                        if (success) {
                            onSaved(fullName.ifBlank { username.ifBlank { email.ifBlank { "Player #$playerId" } } })
                            // Clear password field after success
                            adminPassword = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                if (updateState is UpdateProfileState.Submitting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes")
                }
            }
            if (updateState is UpdateProfileState.Error) {
                val msg = (updateState as UpdateProfileState.Error).message
                Text(text = msg, color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBFDBFE),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun EditProfileScreenPreview() {
    FigcomposeTheme {
        EditProfileScreen(playerId = 1)
    }
}
