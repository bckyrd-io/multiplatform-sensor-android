package com.example.figcompose.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

@Composable
fun rememberStepCounter(): Int {
    val context = LocalContext.current
    var steps by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var initial = -1
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.firstOrNull()?.toInt()?.let { value ->
                    if (initial == -1) initial = value
                    steps = value - initial
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        stepSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return steps
}

@Composable
fun rememberAccelerometer(): Float {
    val context = LocalContext.current
    // Initialize to gravity to avoid showing a large negative deceleration before first reading
    var magnitude by remember { mutableStateOf(SensorManager.GRAVITY_EARTH) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Prefer linear acceleration (gravity removed) if available
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val usingLinear = linearAccel != null
        val targetSensor = linearAccel ?: accelSensor

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { v ->
                    if (v.size >= 3) {
                        val x = v[0]
                        val y = v[1]
                        val z = v[2]
                        val mag = sqrt(x * x + y * y + z * z)
                        magnitude = mag
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        targetSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return magnitude
}

// Richer reading including axis values and whether linear acceleration is used
data class AccelReading(val x: Float, val y: Float, val z: Float, val isLinear: Boolean)

@Composable
fun rememberAccelerationReading(): AccelReading {
    val context = LocalContext.current
    var reading by remember { mutableStateOf(AccelReading(0f, 0f, 0f, isLinear = false)) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val targetSensor = linearAccel ?: accelSensor
        val isLinear = linearAccel != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { v ->
                    if (v.size >= 3) {
                        reading = AccelReading(v[0], v[1], v[2], isLinear)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        targetSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return reading
}

// GPS helpers for speed and distance
data class GpsData(
    val speed: Float = 0f,
    val distance: Float = 0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f
)

@Composable
fun rememberGpsLocation(): GpsData {
    val context = LocalContext.current
    var gpsData by remember { mutableStateOf(GpsData()) }
    var previousLocation by remember { mutableStateOf<Location?>(null) }
    var totalDistance by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                previousLocation?.let { prev ->
                    val d = prev.distanceTo(location)
                    totalDistance += d
                }
                previousLocation = location

                gpsData = GpsData(
                    speed = location.speed,
                    distance = totalDistance,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    return gpsData
}
