package com.example.wear.presentation.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String?,
    val role: String,
    @SerializedName("full_name")
    val fullName: String?,
    val phone: String?,
    @SerializedName("created_at")
    val createdAt: String?
)
