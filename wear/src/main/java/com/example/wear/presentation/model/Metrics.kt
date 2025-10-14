package com.example.wear.presentation.model

import com.google.gson.annotations.SerializedName

// Model for displaying performance data
data class PerformanceData(
    val id: Int,
    @SerializedName("player_id")
    val playerId: Int,
    @SerializedName("session_id")
    val sessionId: Int?,
    @SerializedName("distance_meters")
    val distanceMeters: Double?,
    val speed: Double?,
    val acceleration: Double?,
    val deceleration: Double?,
    @SerializedName("cadence_spm")
    val cadenceSpm: Double?,
    @SerializedName("heart_rate")
    val heartRate: Int?,
    @SerializedName("recorded_at")
    val recordedAt: String?
)

// Model for sending performance data to server
data class PerformanceRequest(
    @SerializedName("player_id")
    val playerId: Int,
    @SerializedName("session_id")
    val sessionId: Int?,
    @SerializedName("distance_meters")
    val distanceMeters: Double?,
    val speed: Double?,
    val acceleration: Double?,
    val deceleration: Double?,
    @SerializedName("cadence_spm")
    val cadenceSpm: Double?,
    @SerializedName("heart_rate")
    val heartRate: Int?
)

// Response from POST /performance
data class PerformanceResponse(
    val success: Boolean,
    val performanceId: Int?
)
