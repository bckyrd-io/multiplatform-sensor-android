package com.example.wear.service

import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for Wear OS network calls
 */
interface ApiService {
    /**
     * List users with optional search and limit
     */
    @GET("users")
    suspend fun getUsers(
        @Query("q") q: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<UserDto>>

    /**
     * Submit performance metrics (speed, heart rate, distance, etc.)
     */
    @POST("performance")
    suspend fun postPerformance(@Body req: PerformanceRequest): Response<PerformanceResponse>

    /**
     * Fetch all sessions (we'll pick the latest by ID on the watch)
     */
    @GET("sessions")
    suspend fun getSessions(): Response<List<SessionDto>>
}

/**
 * User DTO
 */
data class UserDto(
    val id: Int,
    val username: String?,
    val email: String?,
    val role: String?,
    val full_name: String?,
    val phone: String?,
    val created_at: String?
)

/**
 * Performance request/response DTOs
 */
data class PerformanceRequest(
    val player_id: Int,
    val session_id: Int? = null,
    val distance_meters: Double? = null,
    val top_speed: Double? = null,
    val avg_speed: Double? = null,
    val acceleration: Double? = null,
    val deceleration: Double? = null,
    val heart_rate: Int? = null
)

data class PerformanceResponse(
    val success: Boolean,
    val performanceId: Int?
)

/**
 * Session DTO
 */
data class SessionDto(
    val id: Int,
    val title: String?,
    val description: String?,
    val coach_id: Int?,
    val start_time: String?,
    val end_time: String?,
    val session_type: String?,
    val location: String?
)
