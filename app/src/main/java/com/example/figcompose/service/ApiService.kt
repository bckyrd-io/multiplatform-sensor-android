package com.example.figcompose.service

import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for all network calls
 */
interface ApiService {
    // ================ Authentication Endpoints ================
    
    /**
     * Login with email/username and password
     */
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String? = null,
        @Field("username") username: String? = null,
        @Field("password") password: String
    ): Response<AuthResponse>
    
    /**
     * Register a new user
     */
    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("full_name") fullName: String? = null,
        @Field("phone") phone: String? = null,
        @Field("role") role: String = "player"
    ): Response<AuthResponse>
    
    /**
     * Get user by ID
     */
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserDto>
    
    /**
     * List users with optional search and limit
     */
    @GET("users")
    suspend fun getUsers(
        @Query("q") q: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<UserDto>>
    
    /**
     * Update user profile
     */
    @FormUrlEncoded
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Field("username") username: String? = null,
        @Field("email") email: String? = null,
        @Field("full_name") fullName: String? = null,
        @Field("phone") phone: String? = null,
        // Optional role update (coach/player)
        @Field("role") role: String? = null,
        // Optional password change fields
        @Field("current_password") currentPassword: String? = null,
        @Field("new_password") newPassword: String? = null
    ): Response<Map<String, Any?>>
    
    // ================ Session Endpoints ================
    
    /**
     * Basic health check
     */
    @GET("health")
    suspend fun health(): Response<HealthResponse>

    /**
     * Get all sessions
     */
    @GET("sessions")
    suspend fun getSessions(): Response<List<SessionDto>>

    /**
     * Get a single session by ID
     */
    @GET("sessions/{id}")
    suspend fun getSession(@Path("id") id: Int): Response<SessionDto>

    /**
     * Create a new session
     */
    @POST("sessions")
    suspend fun createSession(@Body req: CreateSessionRequest): Response<CreateSessionResponse>
    
    // Add more session-related endpoints as needed

    /**
     * Get consolidated report for a session (session, performances, feedback, survey)
     */
    @GET("reports/{sessionId}")
    suspend fun getReport(@Path("sessionId") sessionId: Int): Response<ReportDto>

    /**
     * Submit a player survey/feedback entry
     */
    @POST("survey")
    suspend fun submitSurvey(@Body req: SubmitSurveyRequest): Response<SubmitSurveyResponse>
}

// ================ DTOs ================

/**
 * Authentication response DTO
 */
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val user: Map<String, Any?>? = null,
    val error: String? = null
)

/**
 * Health check response DTO
 */
data class HealthResponse(
    val status: String?,
    val time: String?
)

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

/**
 * Create session request DTO
 */
data class CreateSessionRequest(
    val title: String? = null,
    val description: String? = null,
    val coach_id: Int? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val session_type: String? = null,
    val location: String? = null,
    // UI-shaped fields supported by the server
    val sessionName: String? = null,
    val name: String? = null,
    val sessionType: String? = null,
    val type: String? = null,
    val date: String? = null,       // MM/DD/YYYY or YYYY-MM-DD
    val startTime: String? = null,  // HH:mm or HH:mm:ss
    val endTime: String? = null,    // HH:mm or HH:mm:ss
    val notes: String? = null
)

/**
 * Create session response DTO
 */
data class CreateSessionResponse(
    val success: Boolean,
    val sessionId: Int?
)

/**
 * Error response DTO
 */
data class ErrorResponse(
    val error: String,
    val message: String? = null,
    val status: Int? = null
)

/**
 * Performance DTO
 */
data class PerformanceDto(
    val id: Int,
    val player_id: Int?,
    val session_id: Int?,
    val distance_meters: Double?,
    val speed: Double?,
    val acceleration: Double?,
    val deceleration: Double?,
    val cadence_spm: Double?,
    val heart_rate: Int?,
    val recorded_at: String?
)

/**
 * Feedback DTO
 */
data class FeedbackDto(
    val id: Int,
    val coach_id: Int?,
    val player_id: Int?,
    val session_id: Int?,
    val notes: String?,
    val created_at: String?
)

/**
 * Survey entry DTO (response is already parsed by the server)
 */
data class SurveyEntryDto(
    val id: Int,
    val player_id: Int?,
    val session_id: Int?,
    val response: Map<String, Any?>?,
    val created_at: String?
)

/**
 * Report DTO combining session, performances, feedback, and survey
 */
data class ReportDto(
    val session: SessionDto?,
    val performances: List<PerformanceDto>,
    val feedback: List<FeedbackDto>,
    val survey: List<SurveyEntryDto>
)

/**
 * Submit survey request/response
 */
data class SubmitSurveyRequest(
    val player_id: Int,
    val session_id: Int? = null,
    val rating: Int? = null,
    val condition: String? = null,
    val performance: String? = null,
    val notes: String? = null,
    val response: Map<String, Any?>? = null
)

data class SubmitSurveyResponse(
    val success: Boolean,
    val surveyId: Int?
)
