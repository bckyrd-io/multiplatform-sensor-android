package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.figcompose.service.PerformanceRequest
import com.example.figcompose.service.RetrofitProvider
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary
import com.example.figcompose.util.RequestMetricsPermissions
import com.example.figcompose.util.AccelReading
import com.example.figcompose.util.rememberAccelerationReading
import com.example.figcompose.util.rememberStepCounter
import com.example.figcompose.util.GpsData
import com.example.figcompose.util.rememberGpsLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    playerId: Int,
    playerName: String,
    sessionId: Int,
    sessionTitle: String,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current

    var hasActivityPermission by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    RequestMetricsPermissions(
        onPermissionsGranted = {
            hasActivityPermission = true
            hasLocationPermission = true
        },
        onPermissionsChanged = { activity, location ->
            hasActivityPermission = activity
            hasLocationPermission = location
        }
    )

    val steps = if (hasActivityPermission) rememberStepCounter() else 0
    val accel: AccelReading = rememberAccelerationReading()
    val gps: GpsData = if (hasLocationPermission) rememberGpsLocation() else GpsData()
    val currentSteps by rememberUpdatedState(steps)
    val currentAccel by rememberUpdatedState(accel)
    val currentGps by rememberUpdatedState(gps)
    val currentHasLocation by rememberUpdatedState(hasLocationPermission)
    val scope = rememberCoroutineScope()

    var elapsed by remember { mutableStateOf(0) }

    var distance by remember { mutableStateOf(0.0) }
    var speed by remember { mutableStateOf(0.0) }
    var cadence by remember { mutableStateOf(0.0) }
    var acceleration by remember { mutableStateOf(0.0) }
    var deceleration by remember { mutableStateOf(0.0) }

    var sumCadence by remember { mutableStateOf(0.0) }
    var sumAccel by remember { mutableStateOf(0.0) }
    var sumDecel by remember { mutableStateOf(0.0) }
    var tickCount by remember { mutableStateOf(0) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsed += 1
            val distanceFromSteps = currentSteps * 0.75
            val distanceFromGps = currentGps.distance.toDouble()
            val speedFromGps = currentGps.speed.toDouble()

            if (currentHasLocation && (distanceFromGps > 0.0 || speedFromGps > 0.0)) {
                distance = distanceFromGps
                speed = speedFromGps
            } else {
                distance = distanceFromSteps
                speed = if (elapsed > 0) distanceFromSteps / elapsed else 0.0
            }
            cadence = if (elapsed > 0) (currentSteps.toDouble() / elapsed) * 60.0 else 0.0

            val ax = currentAccel.x
            val ay = currentAccel.y
            val az = currentAccel.z
            val magnitude = sqrt(ax * ax + ay * ay + az * az)
            val value = if (currentAccel.isLinear) magnitude.toDouble() else kotlin.math.max(0.0, magnitude.toDouble() - 9.81)
            acceleration = value
            // For deceleration, we could track negative changes; here we use a simple heuristic
            deceleration = 0.0

            sumCadence += cadence
            sumAccel += acceleration
            sumDecel += deceleration
            tickCount += 1
        }
    }

    fun stopAndSubmit() {
        if (isSubmitting) return
        isSubmitting = true
        submitError = null
        val avgSpeed = if (elapsed > 0) distance / elapsed else 0.0
        val avgCadence = if (tickCount > 0) sumCadence / tickCount else 0.0
        val avgAccel = if (tickCount > 0) sumAccel / tickCount else 0.0
        val avgDecel = if (tickCount > 0) sumDecel / tickCount else 0.0
        val req = PerformanceRequest(
            player_id = playerId,
            session_id = sessionId,
            distance_meters = distance,
            speed = avgSpeed,
            acceleration = avgAccel,
            deceleration = avgDecel,
            cadence_spm = avgCadence
        )
        val api = RetrofitProvider.api()
        scope.launch {
            try {
                val resp = api.submitPerformance(req)
                if (resp.isSuccessful && (resp.body()?.success == true)) {
                    isSubmitting = false
                    onDone()
                } else {
                    submitError = resp.errorBody()?.string() ?: "Failed to submit"
                    isSubmitting = false
                }
            } catch (e: Exception) {
                submitError = e.message ?: "Error"
                isSubmitting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Metrics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Back")
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text =  sessionTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(8.dp))
                MetricRow("Time (s)", elapsed.toString())
                MetricRow("Steps", steps.toString())
                MetricRow("Distance (m)", String.format("%.2f", distance))
                MetricRow("Speed (m/s)", String.format("%.2f", speed))
                MetricRow("Cadence (spm)", String.format("%.2f", cadence))
                MetricRow("Acceleration", String.format("%.2f", acceleration))
                MetricRow("Deceleration", String.format("%.2f", deceleration))
                if (submitError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = submitError ?: "", color = Color(0xFFDC2626))
                }
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { stopAndSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Stop & Submit", color = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
    }
}
