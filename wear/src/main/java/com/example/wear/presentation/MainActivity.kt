/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.presentation.theme.FigcomposeTheme
import com.example.wear.presentation.screens.SelectPlayerScreen
import com.example.wear.presentation.screens.StartSessionScreen
import com.example.wear.presentation.screens.LiveMetricsScreen
import com.example.wear.util.SessionsManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

private enum class WearScreen { SelectPlayer, StartSession, LiveMetrics }

@Composable
fun WearApp() {
    FigcomposeTheme {
        val scope = rememberCoroutineScope()
        val sessionsManager = remember { SessionsManager() }
        val context = LocalContext.current

        var current by remember { mutableStateOf(WearScreen.SelectPlayer) }
        var playerName by remember { mutableStateOf("") }
        var playerId by remember { mutableStateOf(0) }
        var sessionName by remember { mutableStateOf("Session") }
        var sessionId by remember { mutableStateOf(0) }

        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* no-op */ }

        fun hasAllPermissions(): Boolean = permissions.all { p ->
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            TimeText()

            if (!hasAllPermissions()) {
                Button(onClick = { launcher.launch(permissions) }) {
                    Text("Grant Permissions")
                }
            } else {
                when (current) {
                    WearScreen.SelectPlayer -> SelectPlayerScreen(
                        onPlayerSelected = { id, name ->
                            playerId = id
                            playerName = name
                            scope.launch {
                                val active = sessionsManager.getActiveSession()
                                sessionId = active?.id ?: 0
                                sessionName = active?.title ?: "Session"
                                current = WearScreen.StartSession
                            }
                        }
                    )

                    WearScreen.StartSession -> StartSessionScreen(
                        sessionName = sessionName,
                        playerName = playerName,
                        onStart = { current = WearScreen.LiveMetrics }
                    )

                    WearScreen.LiveMetrics -> LiveMetricsScreen(
                        sessionName = sessionName,
                        playerName = playerName,
                        sessionId = sessionId,
                        playerId = playerId,
                        onEnd = {
                            // Return to selection for a new player/session
                            current = WearScreen.SelectPlayer
                        }
                    )
                }
            }
        }
    }
}
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}