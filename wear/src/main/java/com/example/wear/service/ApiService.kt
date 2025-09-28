package com.example.wear.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("health")
    suspend fun health(): Response<HealthResponse>

    // Sessions
    @GET("sessions")
    suspend fun getSessions(): Response<List<SessionDto>>

    @POST("sessions")
    suspend fun createSession(@Body req: CreateSessionRequest): Response<CreateSessionResponse>

    // Users (for selecting who wears the watch)
    @GET("users")
    suspend fun getUsers(
        @Query("q") q: String? = null,
        @Query("limit") limit: Int? = 50
    ): Response<List<UserDto>>

    // Performance uploads from watch (e.g., heart rate)
    @POST("performance")
    suspend fun postPerformance(@Body req: PerfRequest): Response<PerfResponse>
}

// --- DTOs ---

data class HealthResponse(
    val status: String?,
    val time: String?
)

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

data class CreateSessionRequest(
    val title: String? = null,
    val description: String? = null,
    val coach_id: Int? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val session_type: String? = null,
    val location: String? = null,
    val sessionName: String? = null,
    val name: String? = null,
    val sessionType: String? = null,
    val type: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val notes: String? = null
)

data class CreateSessionResponse(
    val success: Boolean,
    val sessionId: Int?
)

data class UserDto(
    val id: Int,
    val username: String?,
    val email: String?,
    val role: String?,
    val full_name: String?,
    val phone: String?,
    val created_at: String?
)

data class PerfRequest(
    val player_id: Int,
    val session_id: Int? = null,
    val distance_meters: Double? = null,
    val top_speed: Double? = null,
    val avg_speed: Double? = null,
    val acceleration: Double? = null,
    val deceleration: Double? = null,
    val heart_rate: Int? = null
)

data class PerfResponse(
    val success: Boolean,
    val performanceId: Int?
)
