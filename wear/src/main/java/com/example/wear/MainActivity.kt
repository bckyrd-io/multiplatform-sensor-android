package com.example.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.wear.ui.screens.MetricsScreen
import com.example.wear.ui.screens.PlayersListScreen
import com.example.wear.ui.theme.FigcomposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FigcomposeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var selected by remember { mutableStateOf<Pair<Int, String>?>(null) }
                    if (selected == null) {
                        PlayersListScreen(
                            onPlayerSelected = { playerId, playerName ->
                                selected = playerId to playerName
                            }
                        )
                    } else {
                        MetricsScreen(
                            playerId = selected!!.first,
                            playerName = selected!!.second,
                            sessionId = null,
                            onBack = { selected = null }
                        )
                    }
                }
            }
        }
    }
}