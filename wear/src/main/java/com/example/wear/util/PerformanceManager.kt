package com.example.wear.util

import com.example.wear.service.PerfRequest
import com.example.wear.service.RetrofitProvider

class PerformanceManager {
    private val api = RetrofitProvider.api()

    suspend fun postHeartRate(playerId: Int, sessionId: Int, bpm: Int) {
        try {
            api.postPerformance(
                PerfRequest(
                    player_id = playerId,
                    session_id = sessionId,
                    heart_rate = bpm
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun postMetrics(
        playerId: Int,
        sessionId: Int,
        heartRate: Int?,
        topSpeedKmh: Double?,
        distanceMeters: Double?
    ) {
        try {
            api.postPerformance(
                PerfRequest(
                    player_id = playerId,
                    session_id = sessionId,
                    heart_rate = heartRate,
                    top_speed = topSpeedKmh,
                    distance_meters = distanceMeters
                )
            )
        } catch (_: Exception) {}
    }
}
