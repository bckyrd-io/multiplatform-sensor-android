package com.example.wear.util

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import com.example.wear.service.PerformanceRequest
import com.example.wear.service.RetrofitProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MetricsState {
    object Idle : MetricsState()
    object Running : MetricsState()
    data class Error(val message: String) : MetricsState()
}

data class MetricsSnapshot(
    val distanceMeters: Double = 0.0,
    val speedMps: Double = 0.0,
    val avgSpeedMps: Double = 0.0,
    val topSpeedMps: Double = 0.0,
    val acceleration: Double? = null,
    val deceleration: Double? = null,
    val heartRate: Int? = null
)

class MetricsManager(
    private val context: Context,
    private val playerId: Int,
    private val sessionId: Int? = null
) {

    private val api = RetrofitProvider.api()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Exposed state
    private val _status = MutableStateFlow<MetricsState>(MetricsState.Idle)
    val status: StateFlow<MetricsState> = _status

    private val _metrics = MutableStateFlow(MetricsSnapshot())
    val metrics: StateFlow<MetricsSnapshot> = _metrics

    // Location
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location? = null
    private var lastSpeed: Double = 0.0
    private var startTimeMs: Long = 0L

    // Heart rate
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    private var postingJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            handleNewLocation(loc)
        }
    }

    private val heartListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                val hr = event.values.firstOrNull()?.toInt()
                if (hr != null) {
                    _metrics.value = _metrics.value.copy(heartRate = hr)
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (_status.value is MetricsState.Running) return
        _status.value = MetricsState.Running
        startTimeMs = System.currentTimeMillis()
        lastLocation = null
        lastSpeed = 0.0
        _metrics.value = MetricsSnapshot()

        // Start location updates
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        // Start heart rate sensor (if available)
        heartRateSensor?.let {
            sensorManager.registerListener(heartListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Start periodic posting
        postingJob = scope.launch {
            while (_status.value is MetricsState.Running) {
                postMetricsSafely()
                delay(5000L)
            }
        }
    }

    fun stop() {
        if (_status.value !is MetricsState.Running) return
        fusedClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(heartListener)
        postingJob?.cancel()
        postingJob = null
        _status.value = MetricsState.Idle
    }

    private fun handleNewLocation(loc: Location) {
        val nowSpeed = loc.speed.toDouble() // m/s
        val prevLoc = lastLocation
        val distanceInc = if (prevLoc != null) prevLoc.distanceTo(loc).toDouble() else 0.0

        val elapsedSec = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1L) / 1000.0
        val totalDist = _metrics.value.distanceMeters + distanceInc
        val avgSpeed = if (elapsedSec > 0) totalDist / elapsedSec else nowSpeed
        val top = maxOf(_metrics.value.topSpeedMps, nowSpeed)

        val accel = (nowSpeed - lastSpeed) // m/s difference per ~1s sample
        val accVal = if (accel >= 0) accel else null
        val decVal = if (accel < 0) -accel else null

        _metrics.value = _metrics.value.copy(
            distanceMeters = totalDist,
            speedMps = nowSpeed,
            avgSpeedMps = avgSpeed,
            topSpeedMps = top,
            acceleration = accVal,
            deceleration = decVal
        )

        lastSpeed = nowSpeed
        lastLocation = loc
    }

    private suspend fun postMetricsSafely() {
        try {
            val m = _metrics.value
            val req = PerformanceRequest(
                player_id = playerId,
                session_id = sessionId,
                distance_meters = m.distanceMeters,
                top_speed = m.topSpeedMps,
                avg_speed = m.avgSpeedMps,
                acceleration = m.acceleration,
                deceleration = m.deceleration,
                heart_rate = m.heartRate
            )
            val resp = api.postPerformance(req)
            if (!resp.isSuccessful) {
                // Keep running but update error state snapshot for UI if needed
                // Do not switch status out of Running
            }
        } catch (e: Exception) {
            // Ignore transient errors; UI remains in running
        }
    }

    fun close() {
        stop()
        scope.cancel()
    }
}
