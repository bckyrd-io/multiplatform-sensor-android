package com.example.wear.util

import com.example.wear.service.RetrofitProvider
import com.example.wear.service.SessionDto

class SessionsManager {
    private val api = RetrofitProvider.api()

    suspend fun getLatestSession(): SessionDto? {
        val resp = api.getSessions()
        if (!resp.isSuccessful) return null
        val list = resp.body().orEmpty()
        // Assuming server returns chronological or we just pick the last by id
        return list.maxByOrNull { it.id }
    }

    suspend fun getActiveSession(): SessionDto? {
        val resp = api.getSessions()
        if (!resp.isSuccessful) return null
        val list = resp.body().orEmpty()
        val ongoing = list.filter { it.end_time == null }
        return (ongoing.maxByOrNull { it.id }) ?: list.maxByOrNull { it.id }
    }
}
