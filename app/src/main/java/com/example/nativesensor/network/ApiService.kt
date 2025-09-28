package com.example.nativesensor.network

import com.example.nativesensor.model.SensorData
import com.example.nativesensor.model.UserStats
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Call

// Data classes for request and response

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserData? = null
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

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

data class SensorDataListResponse(
    val success: Boolean,
    val sensorData: List<Map<String, Any>>
)

data class UserStatsResponse(
    val success: Boolean,
    val totalStats: Map<String, Any>,
    val todayStats: Map<String, Any>
)

data class UsersResponse(
    val success: Boolean,
    val users: List<Map<String, Any>>
)

interface ApiService {
    @POST("/api/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/sensor-data")
    fun submitSensorData(@Body request: SensorDataRequest): Call<SensorDataResponse>

    @GET("/api/sensor-data")
    fun getSensorData(
        @Query("user_role") userRole: String,
        @Query("current_user_id") currentUserId: String? = null
    ): Call<SensorDataListResponse>

    @GET("/api/sensor-data/{userId}")
    fun getSensorDataForUser(
        @Query("user_role") userRole: String,
        @Query("current_user_id") currentUserId: String? = null
    ): Call<SensorDataListResponse>

    @GET("/api/user-stats")
    fun getUserStats(
        @Query("user_role") userRole: String,
        @Query("current_user_id") currentUserId: String? = null
    ): Call<UserStatsResponse>

    @GET("/api/user-stats/{userId}")
    fun getUserStatsForUser(
        @Query("user_role") userRole: String,
        @Query("current_user_id") currentUserId: String? = null
    ): Call<UserStatsResponse>

    @GET("/api/users")
    fun getAllUsers(): Call<UsersResponse>
} 