package com.example.wear.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

data class SensorDataRequest(
    val user_id: Int,
    val heart_rate: Int? = null,
    val steps: Int? = null,
    val calories: Int? = null,
    val activity_type: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class SensorDataResponse(
    val success: Boolean,
    val message: String
)

interface ApiService {
    @POST("/api/sensor-data")
    fun submitSensorData(@Body request: SensorDataRequest): Call<SensorDataResponse>
} 