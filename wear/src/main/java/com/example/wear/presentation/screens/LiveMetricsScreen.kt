package com.example.wear.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wear.util.HeartRateSensor
import com.example.wear.util.SpeedSensor
import com.example.wear.util.PerformanceManager
import kotlinx.coroutines.delay

@Composable
fun LiveMetricsScreen(
    sessionName: String,
    playerName: String,
    sessionId: Int,
    playerId: Int,
    onEnd: () -> Unit
) {
    val context = LocalContext.current
    val perfManager = remember { PerformanceManager() }

    var heartRate by remember { mutableStateOf<Int?>(null) }
    var speedKmh by remember { mutableStateOf<Double?>(null) }
    var distanceMeters by remember { mutableStateOf<Double?>(null) }

    val hrSensor = remember { HeartRateSensor(context) }
    val speedSensor = remember { SpeedSensor(context) }

    // Start/stop sensors with lifecycle of this screen
    DisposableEffect(Unit) {
        hrSensor.start { bpm -> heartRate = bpm }
        speedSensor.start { kmh, dist ->
            speedKmh = kmh
            distanceMeters = dist
        }
        onDispose {
            hrSensor.stop()
            speedSensor.stop()
        }
    }

    // Periodically upload metrics
    LaunchedEffect(sessionId, playerId) {
        while (true) {
            perfManager.postMetrics(
                playerId = playerId,
                sessionId = sessionId,
                heartRate = heartRate,
                topSpeedKmh = speedKmh,
                distanceMeters = distanceMeters
            )
            delay(10_000)
        }
    }

    Card(
        onClick = {},
        enabled = false,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = sessionName,
                style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = playerName,
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))

            MetricRow(title = "Heart Rate", value = heartRate?.let { "$it bpm" } ?: "--")
            MetricRow(title = "Speed", value = speedKmh?.let { String.format("%.1f km/h", it) } ?: "--")
            MetricRow(title = "Distance", value = distanceMeters?.let { String.format("%.2f km", it / 1000) } ?: "--")

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onEnd,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Text(text = "End Session")
            }
        }
    }
}

@Composable
private fun MetricRow(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = title, style = MaterialTheme.typography.caption3)
        Text(text = value, style = MaterialTheme.typography.caption2, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
    }
}
