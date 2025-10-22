package com.example.figcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.figcompose.ui.screens.*
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.util.AuthManager
import com.example.figcompose.util.AuthState
import com.example.figcompose.util.SessionManager
import kotlinx.coroutines.launch

sealed class Screen {
    object Landing : Screen()
    object Login : Screen()
    object SignUp : Screen()
    object Dashboard : Screen()
    object CreateSession : Screen()
    data class SessionDetails(val sessionId: Int) : Screen()
    object Users : Screen()
    data class Profile(val playerId: Int, val playerName: String) : Screen()
    data class EditProfile(val playerId: Int, val playerName: String) : Screen()
    data class SurveyFeedback(val playerId: Int, val playerName: String, val sessionId: Int? = null) : Screen()
    data class PlayerProgress(val playerId: Int, val playerName: String, val sessionId: Int? = null) : Screen()
    data class PlayerHome(val playerId: Int, val playerName: String) : Screen()
    data class Metrics(val playerId: Int, val playerName: String, val sessionId: Int, val sessionTitle: String) : Screen()
    data class CoachFeedback(val coachId: Int?, val playerId: Int, val playerName: String, val sessionId: Int) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val authManager = remember { AuthManager(context) }
            val sessionManager = remember { SessionManager(context) }
            val currentScreen = remember { mutableStateOf<Screen>(Screen.Landing) }
            var showErrorDialog by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()
            
            // Check auth state when the app starts
            LaunchedEffect(Unit) {
                authManager.checkAuthState()
                // If user is already authenticated, route by role
                if (authManager.isLoggedIn()) {
                    val user = authManager.currentUser.value
                    val id = (user?.get("id") as? Number)?.toInt() ?: -1
                    val role = user?.get("role") as? String
                    val name = (user?.get("fullName") as? String)
                        ?: (user?.get("username") as? String)
                        ?: if (id != -1) "User #$id" else "User"
                    currentScreen.value = if (role == "player") Screen.PlayerHome(id, name) else Screen.Dashboard
                }
            }
            
            // Show error dialog if there's an error
            showErrorDialog?.let { errorMessage ->
                AlertDialog(
                    onDismissRequest = { showErrorDialog = null },
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = null }) {
                            Text("OK")
                        }
                    }
                )
            }
            
            FigcomposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen.value) {
                        is Screen.Landing -> {
                            if (authManager.isLoggedIn()) {
                                currentScreen.value = Screen.Dashboard
                            } else {
                                LandingScreen(
                                    onLogin = { currentScreen.value = Screen.Login },
                                    onSignUp = { currentScreen.value = Screen.SignUp }
                                )
                            }
                        }
                        is Screen.Login -> {
                            LoginScreen(
                                onBack = { currentScreen.value = Screen.Landing },
                                onLogin = { email, password ->
                                    coroutineScope.launch {
                                        authManager.login(email, password) { success, errorMessage ->
                                            if (success) {
                                                val user = authManager.currentUser.value
                                                val id = (user?.get("id") as? Number)?.toInt() ?: -1
                                                val role = user?.get("role") as? String
                                                val name = (user?.get("fullName") as? String)
                                                    ?: (user?.get("username") as? String)
                                                    ?: if (id != -1) "User #$id" else "User"
                                                currentScreen.value = if (role == "player") Screen.PlayerHome(id, name) else Screen.Dashboard
                                            } else {
                                                showErrorDialog = errorMessage ?: "Login failed. Please try again."
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        is Screen.SignUp -> {
                            SignUpScreen(
                                onBack = { currentScreen.value = Screen.Landing },
                                onSignUp = { name, email, password ,fullName, phone ->
                                    coroutineScope.launch {
                                        authManager.register(
                                            username = name,
                                            email = email,
                                            password = password,
                                            fullName = fullName,
                                            phone = phone
                                        ) { success, errorMessage ->
                                            if (success) {
                                                val user = authManager.currentUser.value
                                                val id = (user?.get("id") as? Number)?.toInt() ?: -1
                                                val role = user?.get("role") as? String
                                                val displayName = (user?.get("fullName") as? String)
                                                    ?: (user?.get("username") as? String)
                                                    ?: if (id != -1) "User #$id" else "User"
                                                currentScreen.value = if (role == "player") Screen.PlayerHome(id, displayName) else Screen.Dashboard
                                            } else {
                                                showErrorDialog = errorMessage ?: "Registration failed. Please try again."
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                onSettings = {
                                    authManager.logout()
                                    currentScreen.value = Screen.Landing
                                },
                                onAdd = { currentScreen.value = Screen.CreateSession },
                                onSessionClick = { id -> currentScreen.value = Screen.SessionDetails(id) },
                                onUsersClick = { currentScreen.value = Screen.Users }
                            )
                        }
                        is Screen.Profile -> {
                            com.example.figcompose.ui.screens.ProfileScreen(
                                playerId = screen.playerId,
                                playerName = screen.playerName,
                                onBack = { currentScreen.value = Screen.Users },
                                onEdit = { currentScreen.value = Screen.EditProfile(screen.playerId, screen.playerName) },
                                onSubmitFeedback = { currentScreen.value = Screen.SurveyFeedback(screen.playerId, screen.playerName, null) },
                                showEdit = true,
                                showSubmitFeedback = false
                            )
                        }
                        is Screen.EditProfile -> {
                            com.example.figcompose.ui.screens.EditProfileScreen(
                                playerId = screen.playerId,
                                onBack = { currentScreen.value = Screen.Profile(screen.playerId, screen.playerName) },
                                onSaved = { updatedName ->
                                    currentScreen.value = Screen.Profile(screen.playerId, updatedName)
                                }
                            )
                        }
                        is Screen.SurveyFeedback -> {
                            com.example.figcompose.ui.screens.SurveyFeedbackScreen(
                                playerId = screen.playerId,
                                playerName = screen.playerName,
                                sessionId = screen.sessionId,
                                onBack = {
                                    val role = authManager.currentUser.value?.get("role") as? String
                                    if (role == "player") currentScreen.value = Screen.PlayerHome(screen.playerId, screen.playerName)
                                    else currentScreen.value = Screen.Profile(screen.playerId, screen.playerName)
                                },
                                onSubmitSuccess = {
                                    val role = authManager.currentUser.value?.get("role") as? String
                                    if (role == "player") currentScreen.value = Screen.PlayerHome(screen.playerId, screen.playerName)
                                    else currentScreen.value = Screen.Profile(screen.playerId, screen.playerName)
                                }
                            )
                        }
                        is Screen.Users -> {
                            UsersScreen(
                                onBack = { currentScreen.value = Screen.Dashboard },
                                onUserSelected = { playerId, playerName ->
                                    currentScreen.value = Screen.Profile(playerId, playerName)
                                }
                            )
                        }
                        is Screen.CreateSession -> {
                            CreateSessionScreen(
                                onBack = { currentScreen.value = Screen.Dashboard },
                                onCreateSession = { name, type, startTime, endTime, location, notes ->
                                    coroutineScope.launch {
                                        sessionManager.createSession(
                                            name = name,
                                            type = type,
                                            startTime = startTime,
                                            endTime = endTime,
                                            location = location,
                                            notes = notes
                                        ) { success, _, error ->
                                            if (success) {
                                                currentScreen.value = Screen.Dashboard
                                            } else {
                                                showErrorDialog = error ?: "Failed to create session. Please try again."
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        is Screen.PlayerProgress -> {
                            PlayerProgressScreen(
                                playerId = screen.playerId,
                                playerName = screen.playerName,
                                sessionId = screen.sessionId,
                                onBack = {
                                    val role = authManager.currentUser.value?.get("role") as? String
                                    if (role == "player") currentScreen.value = Screen.PlayerHome(screen.playerId, screen.playerName)
                                    else currentScreen.value = Screen.Dashboard
                                },
                                canAddFeedback = (authManager.currentUser.value?.get("role") as? String) == "coach",
                                onAddFeedback = {
                                    val coachId = (authManager.currentUser.value?.get("id") as? Number)?.toInt()
                                    val sid = screen.sessionId ?: return@PlayerProgressScreen
                                    currentScreen.value = Screen.CoachFeedback(coachId, screen.playerId, screen.playerName, sid)
                                }
                            )
                        }
                        is Screen.SessionDetails -> {
                            SessionDetailsScreen(
                                sessionId = screen.sessionId,
                                onBack = { currentScreen.value = Screen.Dashboard },
                                onPlayerSelected = { playerId, playerName, sessionId -> currentScreen.value = Screen.PlayerProgress(playerId, playerName, sessionId) }
                            )
                        }
                        is Screen.PlayerHome -> {
                            PlayerHomeScreen(
                                playerId = screen.playerId,
                                playerName = screen.playerName,
                                onBack = { /* Player home acts as root for players */ },
                                onSubmitFeedback = { sid ->
                                    currentScreen.value = Screen.SurveyFeedback(screen.playerId, screen.playerName, sid)
                                },
                                onSettings = {
                                    authManager.logout()
                                    currentScreen.value = Screen.Landing
                                },
                                onOpenMetrics = { sid, stitle ->
                                    currentScreen.value = Screen.Metrics(screen.playerId, screen.playerName, sid, stitle)
                                }
                            )
                        }
                        is Screen.Metrics -> {
                            MetricsScreen(
                                playerId = screen.playerId,
                                playerName = screen.playerName,
                                sessionId = screen.sessionId,
                                sessionTitle = screen.sessionTitle,
                                onBack = { currentScreen.value = Screen.PlayerHome(screen.playerId, screen.playerName) },
                                onDone = { currentScreen.value = Screen.PlayerHome(screen.playerId, screen.playerName) }
                            )
                        }
                        is Screen.CoachFeedback -> {
                            CoachFeedbackScreen(
                                coachId = screen.coachId,
                                playerId = screen.playerId,
                                playerName = screen.playerName,
                                sessionId = screen.sessionId,
                                onBack = { currentScreen.value = Screen.PlayerProgress(screen.playerId, screen.playerName, screen.sessionId) },
                                onSubmitted = { currentScreen.value = Screen.PlayerProgress(screen.playerId, screen.playerName, screen.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLandingScreen() {
    FigcomposeTheme {
        LandingScreen(
            onLogin = {},
            onSignUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateSessionScreen() {
    FigcomposeTheme {
        CreateSessionScreen(
            onBack = {},
            onCreateSession = { _, _, _, _, _, _ -> }
        )
    }
}