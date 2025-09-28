package com.example.figcompose.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.figcompose.service.ApiService
import com.example.figcompose.service.RetrofitProvider
import com.example.figcompose.service.SessionDto
import com.example.figcompose.service.UserDto
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Loaded(
        val sessions: List<SessionDto>,
        val users: List<UserDto>,
    ) : DashboardState() {
        val sessionCount: Int get() = sessions.size
        val userCount: Int get() = users.size
    }
    data class Error(val message: String) : DashboardState()
}

class DashboardManager(private val context: Context) : ViewModel() {
    private val api: ApiService = RetrofitProvider.api()

    val state = mutableStateOf<DashboardState>(DashboardState.Idle)

    fun loadOverview(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                state.value = DashboardState.Loading

                val sessionsDeferred = async { api.getSessions() }
                val usersDeferred = async { api.getUsers(limit = 200) }

                val sessionsResp = sessionsDeferred.await()
                val usersResp = usersDeferred.await()

                if (sessionsResp.isSuccessful && usersResp.isSuccessful) {
                    val sessions = sessionsResp.body().orEmpty()
                    val users = usersResp.body().orEmpty()
                    state.value = DashboardState.Loaded(sessions = sessions, users = users)
                    onComplete(true, null)
                } else {
                    val errorMsg = buildString {
                        if (!sessionsResp.isSuccessful) {
                            append(parseErrorBody(sessionsResp.errorBody()?.string()))
                        }
                        if (!usersResp.isSuccessful) {
                            if (isNotEmpty()) append("; ")
                            append(parseErrorBody(usersResp.errorBody()?.string()))
                        }
                    }.ifBlank { "Failed to fetch dashboard data" }
                    state.value = DashboardState.Error(errorMsg)
                    onComplete(false, errorMsg)
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                state.value = DashboardState.Error(message)
                onComplete(false, message)
            }
        }
    }

    private fun parseErrorBody(body: String?): String {
        return try {
            if (body.isNullOrBlank()) return ""
            JSONObject(body).optString("error").ifBlank { body }
        } catch (ex: Exception) {
            body ?: ""
        }
    }
}
