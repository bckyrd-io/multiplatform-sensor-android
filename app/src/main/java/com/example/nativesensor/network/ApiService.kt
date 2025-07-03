package com.example.nativesensor.network

import retrofit2.http.Body
import retrofit2.http.POST
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
    val message: String
)

interface ApiService {
    @POST("/api/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>
} 