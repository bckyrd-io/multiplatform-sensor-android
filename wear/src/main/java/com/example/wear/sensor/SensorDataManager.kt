package com.example.wear.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Task
import com.example.wear.network.ApiClient
import com.example.wear.network.SensorDataRequest
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SensorDataManager(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()
    
    private var currentUserId: Int = 0
    private var isCollecting = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun setUserId(userId: Int) {
        currentUserId = userId
    }
    
    fun startDataCollection() {
        if (isCollecting) return
        isCollecting = true
        
        scope.launch {
            while (isCollecting) {
                try {
                    val sensorData = collectSensorData()
                    sendSensorData(sensorData)
                    delay(30000) // Send data every 30 seconds
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(60000) // Wait 1 minute on error
                }
            }
        }
    }
    
    fun stopDataCollection() {
        isCollecting = false
    }
    
    private suspend fun collectSensorData(): SensorDataRequest {
        val location = getCurrentLocation()
        val fitnessData = getFitnessData()
        
        return SensorDataRequest(
            user_id = currentUserId,
            heart_rate = fitnessData.heartRate,
            steps = fitnessData.steps,
            calories = fitnessData.calories,
            activity_type = fitnessData.activityType,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
    }
    
    private suspend fun getCurrentLocation(): Location? = suspendCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            continuation.resume(null)
            return@suspendCoroutine
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
    
    private suspend fun getFitnessData(): FitnessData = suspendCoroutine { continuation ->
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(1) // Last hour
        
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.TYPE_HEART_RATE_BPM)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        
        Fitness.getHistoryClient(context, fitnessOptions)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val fitnessData = parseFitnessData(response)
                continuation.resume(fitnessData)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
    
    private fun parseFitnessData(response: DataReadResponse): FitnessData {
        var steps = 0
        var heartRate = 0
        var calories = 0
        var activityType = "unknown"
        
        for (dataSet in response.dataSets) {
            for (dataPoint in dataSet.dataPoints) {
                when (dataSet.dataType) {
                    DataType.TYPE_STEP_COUNT_DELTA -> {
                        steps += dataPoint.getValue(DataType.FIELD_STEPS).asInt()
                    }
                    DataType.TYPE_HEART_RATE_BPM -> {
                        heartRate = dataPoint.getValue(DataType.FIELD_BPM).asInt()
                    }
                    DataType.TYPE_CALORIES_EXPENDED -> {
                        calories += dataPoint.getValue(DataType.FIELD_CALORIES).asFloat().toInt()
                    }
                }
            }
        }
        
        return FitnessData(steps, heartRate, calories, activityType)
    }
    
    private suspend fun sendSensorData(sensorData: SensorDataRequest) {
        try {
            val response = ApiClient.apiService.submitSensorData(sensorData).execute()
            if (!response.isSuccessful || !response.body()?.success!!) {
                throw Exception("Failed to send sensor data: ${response.body()?.message}")
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }
    
    data class FitnessData(
        val steps: Int,
        val heartRate: Int,
        val calories: Int,
        val activityType: String
    )
} 