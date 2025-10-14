package com.example.wear.presentation.util

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

/**
 * Composable function to access the heart rate sensor
 */
@Composable
fun rememberHeartRateSensor(): Int {
    val context = LocalContext.current
    var heartRate by remember { mutableStateOf(0) }
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.values.isNotEmpty()) {
                        heartRate = it.values[0].toInt()
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        heartRateSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    return heartRate
}

/**
 * Composable function to access the step counter sensor
 */
@Composable
fun rememberStepCounter(): Int {
    val context = LocalContext.current
    var steps by remember { mutableStateOf(0) }
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        var initialSteps = -1
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.values.isNotEmpty()) {
                        val currentSteps = it.values[0].toInt()
                        if (initialSteps == -1) {
                            initialSteps = currentSteps
                        }
                        steps = currentSteps - initialSteps
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        stepSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    return steps
}

/**
 * Composable function to access the accelerometer sensor
 * Returns acceleration magnitude
 */
@Composable
fun rememberAccelerometer(): Float {
    val context = LocalContext.current
    var acceleration by remember { mutableStateOf(0f) }
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.values.size >= 3) {
                        val x = it.values[0]
                        val y = it.values[1]
                        val z = it.values[2]
                        // Calculate magnitude
                        acceleration = kotlin.math.sqrt(x * x + y * y + z * z)
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        accelerometerSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    return acceleration
}

/**
 * Data class to hold GPS location info
 */
data class GpsData(
    val speed: Float = 0f,        // Speed in m/s from GPS
    val distance: Float = 0f,     // Total distance in meters
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f
)

/**
 * Composable function to access GPS location for accurate speed and distance
 * Returns real-time GPS data
 */
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
                // Calculate distance from previous location
                previousLocation?.let { prev ->
                    val distance = prev.distanceTo(location)
                    totalDistance += distance
                }
                previousLocation = location
                
                gpsData = GpsData(
                    speed = location.speed,           // m/s from GPS
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
                1000L,  // Update every 1 second
                1f,     // Minimum distance: 1 meter
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
