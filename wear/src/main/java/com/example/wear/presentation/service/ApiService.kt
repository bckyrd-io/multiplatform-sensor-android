package com.example.wear.presentation.service

import com.example.wear.presentation.model.PerformanceData
import com.example.wear.presentation.model.PerformanceRequest
import com.example.wear.presentation.model.PerformanceResponse
import com.example.wear.presentation.model.Session
import com.example.wear.presentation.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    
    // Get all users (filter role='player' on client side)
    @GET("users")
    suspend fun getUsers(): List<User>
    
    // Get all sessions
    @GET("sessions")
    suspend fun getSessions(): List<Session>
    
    // Get performance data for a specific player
    @GET("performance/{playerId}")
    suspend fun getPlayerPerformance(@Path("playerId") playerId: Int): List<PerformanceData>
    
    // Send performance data to server
    @POST("performance")
    suspend fun submitPerformance(@Body performance: PerformanceRequest): PerformanceResponse
}
