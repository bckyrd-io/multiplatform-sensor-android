package com.example.wear.presentation.model

import com.google.gson.annotations.SerializedName

data class Session(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("coach_id")
    val coachId: Int?,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("session_type")
    val sessionType: String?,
    val location: String?,
    @SerializedName("created_at")
    val createdAt: String?
)
