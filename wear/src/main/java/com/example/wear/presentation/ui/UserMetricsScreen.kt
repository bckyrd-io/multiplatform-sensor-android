package com.example.wear.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.example.wear.presentation.model.PerformanceRequest
import com.example.wear.presentation.service.RetrofitClient
import com.example.wear.presentation.util.RequestSensorPermissions
import com.example.wear.presentation.util.rememberAccelerometer
import com.example.wear.presentation.util.rememberHeartRateSensor
import com.example.wear.presentation.util.rememberStepCounter
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun UserMetricsScreen(
    navController: NavHostController,
    playerId: Int,
    playerName: String,
    sessionId: Int,
    sessionTitle: String
) {
    // Request sensor permissions
    RequestSensorPermissions()
    
    // Real sensor data from watch
    val heartRate = rememberHeartRateSensor()
    val steps = rememberStepCounter()
    val accelerationMagnitude = rememberAccelerometer()
    
    // State for calculated metrics
    var distanceMeters by remember { mutableStateOf(0.0) }
    var speed by remember { mutableStateOf(0.0) }
    var cadence by remember { mutableStateOf(0.0) }
    var acceleration by remember { mutableStateOf(0.0) }
    var deceleration by remember { mutableStateOf(0.0) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    
    var isSending by remember { mutableStateOf(false) }
    var sendSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Calculate metrics from sensors
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsedSeconds++
            
            // Estimate distance from steps (average stride: ~0.75m)
            distanceMeters = steps * 0.75
            
            // Calculate speed (m/s) from distance and time
            speed = if (elapsedSeconds > 0) distanceMeters / elapsedSeconds else 0.0
            
            // Calculate cadence (steps per minute)
            cadence = if (elapsedSeconds > 0) (steps.toDouble() / elapsedSeconds) * 60 else 0.0
            
            // Use accelerometer for acceleration/deceleration estimates
            acceleration = maxOf(0.0, (accelerationMagnitude - 9.8).toDouble())
            deceleration = minOf(0.0, (accelerationMagnitude - 9.8).toDouble()).absoluteValue
        }
    }
    
    // Function to send accumulated performance data to server
    fun sendPerformanceData() {
        scope.launch {
            isSending = true
            try {
                val perfData = PerformanceRequest(
                    playerId = playerId,
                    sessionId = sessionId,
                    distanceMeters = distanceMeters,
                    speed = speed,
                    acceleration = acceleration,
                    deceleration = deceleration,
                    cadenceSpm = cadence,
                    heartRate = heartRate
                )
                
                val response = RetrofitClient.apiService.submitPerformance(perfData)
                if (response.success) {
                    sendSuccess = true
                    kotlinx.coroutines.delay(1000)
                    // Navigate back to user selection
                    navController.popBackStack("userSelection", inclusive = false)
                }
                isSending = false
            } catch (e: Exception) {
                errorMessage = "Failed to send data: ${e.message}"
                isSending = false
            }
        }
    }
    
    Scaffold(
        timeText = { TimeText() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: "Error",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp)
            ) {
                // Player name
                item {
                    Text(
                        text = playerName,
                        style = MaterialTheme.typography.title3,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = MaterialTheme.colors.primary
                    )
                }
                
                // Session title
                item {
                    Text(
                        text = sessionTitle,
                        style = MaterialTheme.typography.caption1,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colors.primary
                    )
                }
                
                // Real-time metrics
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Distance", style = MaterialTheme.typography.caption1, color = Color.Black)
                        Text(
                            text = "${distanceMeters.toInt()} m",
                            style = MaterialTheme.typography.title2,
                            color = Color.Black
                        )
                    }
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Heart Rate", style = MaterialTheme.typography.caption1, color = Color.Black)
                        Text(
                            text = "$heartRate bpm",
                            style = MaterialTheme.typography.title2,
                            color = Color.Black
                        )
                    }
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Speed", style = MaterialTheme.typography.caption1, color = Color.Black)
                        Text(
                            text = String.format("%.1f m/s", speed),
                            style = MaterialTheme.typography.title2,
                            color = Color.Black
                        )
                    }
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Steps", style = MaterialTheme.typography.caption1, color = Color.Black)
                        Text(
                            text = "$steps",
                            style = MaterialTheme.typography.title2,
                            color = Color.Black
                        )
                    }
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Cadence", style = MaterialTheme.typography.caption1, color = Color.Black)
                        Text(
                            text = "${cadence.toInt()} spm",
                            style = MaterialTheme.typography.title2,
                            color = Color.Black
                        )
                    }
                }
                
                // Exit button to send performance data
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Button(
                        onClick = { sendPerformanceData() },
                        enabled = !isSending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else if (sendSuccess) {
                            Text("Success!")
                        } else {
                            Text("Send & Exit")
                        }
                    }
                }
            }
        }
    }
}
