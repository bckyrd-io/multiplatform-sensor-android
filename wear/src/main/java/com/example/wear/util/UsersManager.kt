package com.example.wear.util

import com.example.wear.service.RetrofitProvider
import com.example.wear.service.UserDto

class UsersManager {
    private val api = RetrofitProvider.api()

    suspend fun getPlayers(limit: Int = 200): List<UserDto> {
        val resp = api.getUsers(limit = limit)
        if (!resp.isSuccessful) return emptyList()
        val all = resp.body().orEmpty()
        return all.filter { it.role?.equals("player", ignoreCase = true) == true }
    }
}
