package com.example.wear.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.wear.service.RetrofitProvider
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.example.wear.util.MetricsManager
import com.example.wear.util.MetricsState

@Composable
fun MetricsScreen(
    playerId: Int,
    playerName: String,
    sessionId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val api = remember { RetrofitProvider.api() }
    val resolvedSessionIdState = remember { mutableStateOf<Int?>(sessionId) }
    val sessionError = remember { mutableStateOf<String?>(null) }

    // Resolve latest session if not provided
    LaunchedEffect(Unit) {
        if (resolvedSessionIdState.value == null) {
            try {
                val resp = api.getSessions()
                if (resp.isSuccessful) {
                    val sessions = resp.body().orEmpty()
                    val latest = sessions.maxByOrNull { it.id }
                    resolvedSessionIdState.value = latest?.id
                    sessionError.value = null
                } else {
                    sessionError.value = "Failed to fetch sessions (${resp.code()})"
                }
            } catch (e: Exception) {
                sessionError.value = e.message ?: "Failed to fetch sessions"
            }
        }
    }

    val resolvedSessionId = resolvedSessionIdState.value
    val metricsManager = remember(playerId, resolvedSessionId) {
        resolvedSessionId?.let { MetricsManager(context, playerId, it) }
    }
    DisposableEffect(metricsManager) { onDispose { metricsManager?.close() } }

    val status by (metricsManager?.status?.collectAsState() ?: remember { mutableStateOf(MetricsState.Idle) })
    val snapshot by (metricsManager?.metrics?.collectAsState() ?: remember { mutableStateOf(com.example.wear.util.MetricsSnapshot()) })

    val listState = rememberScalingLazyListState()

    val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BODY_SENSORS
    )

    val hasAllPermissions = remember { mutableStateOf(false) }

    fun computePermissionState(): Boolean {
        return requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(Unit) {
        hasAllPermissions.value = computePermissionState()
    }

    val requestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasAllPermissions.value = computePermissionState()
    }

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
                Text(text = "${playerName}")
            }

            // Session info / resolution
            if (resolvedSessionId == null) {
                item {
                    Chip(
                        onClick = {
                            // retry fetch
                            sessionError.value = null
                            resolvedSessionIdState.value = null
                            // trigger LaunchedEffect by reassigning value
                            // Note: recomposition occurs and effect will run again
                        },
                        label = { Text(sessionError.value?.let { "Retry: $it" } ?: "Resolving session…") },
                        secondaryLabel = { Text("Using latest session") },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            } else {
                item { Chip(onClick = { /* no-op */ }, label = { Text("Session #$resolvedSessionId") }) }
            }

            // Permissions
            if (!hasAllPermissions.value) {
                item {
                    Chip(
                        onClick = {
                            activity?.let {
                                requestLauncher.launch(requiredPermissions)
                            }
                        },
                        label = { Text("Grant permissions") },
                        secondaryLabel = { Text("Location & Heart rate") },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }

            // Start/Stop toggle
            item {
                val running = status is MetricsState.Running
                ToggleChip(
                    checked = running,
                    onCheckedChange = { checked ->
                        if (resolvedSessionId == null) {
                            // wait for session resolution
                            return@ToggleChip
                        }
                        if (!hasAllPermissions.value) {
                            activity?.let { requestLauncher.launch(requiredPermissions) }
                            return@ToggleChip
                        }
                        metricsManager?.let { if (checked) it.start() else it.stop() }
                    },
                    label = { Text(if (running) "Streaming…" else "Start streaming") },
                    toggleControl = {
                        Switch(checked = running)
                    }
                )
            }

            // Live metrics
            items(listOf(
                "Speed" to String.format("%.1f km/h", snapshot.speedMps * 3.6),
                "Avg Speed" to String.format("%.1f km/h", snapshot.avgSpeedMps * 3.6),
                "Top Speed" to String.format("%.1f km/h", snapshot.topSpeedMps * 3.6),
                "Distance" to String.format("%.0f m", snapshot.distanceMeters),
                "Heart Rate" to (snapshot.heartRate?.toString() ?: "—")
            )) { (label, value) ->
                Chip(
                    onClick = { /* no-op */ },
                    label = { Text(label) },
                    secondaryLabel = { Text(value) }
                )
            }

            // Back
            item {
                Button(onClick = onBack) { Text("Back") }
            }
        }
    }
}
