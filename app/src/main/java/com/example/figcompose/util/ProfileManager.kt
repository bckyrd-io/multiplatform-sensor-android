package com.example.figcompose.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.figcompose.service.ApiService
import com.example.figcompose.service.RetrofitProvider
import com.example.figcompose.service.UserDto
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed class PlayerProfileState {
    object Idle : PlayerProfileState()
    object Loading : PlayerProfileState()
    data class Loaded(val user: UserDto) : PlayerProfileState()
    data class Error(val message: String) : PlayerProfileState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Submitting : UpdateProfileState()
    data class Success(val user: UserDto?) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

class ProfileManager(context: Context) : ViewModel() {
    private val api: ApiService = RetrofitProvider.api()

    val state = mutableStateOf<PlayerProfileState>(PlayerProfileState.Idle)
    val updateState = mutableStateOf<UpdateProfileState>(UpdateProfileState.Idle)

    fun loadUser(userId: Int, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                state.value = PlayerProfileState.Loading
                val resp = api.getUser(userId)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null) {
                        state.value = PlayerProfileState.Loaded(body)
                        onComplete(true, null)
                    } else {
                        val msg = "User not found"
                        state.value = PlayerProfileState.Error(msg)
                        onComplete(false, msg)
                    }
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to load user" }
                    state.value = PlayerProfileState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                state.value = PlayerProfileState.Error(msg)
                onComplete(false, msg)
            }
        }
    }

    fun updateUser(
        userId: Int,
        username: String? = null,
        email: String? = null,
        fullName: String? = null,
        phone: String? = null,
        role: String? = null,
        currentPassword: String? = null,
        newPassword: String? = null,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                updateState.value = UpdateProfileState.Submitting
                val resp = api.updateUser(
                    userId = userId.toLong(),
                    username = username,
                    email = email,
                    fullName = fullName,
                    phone = phone,
                    role = role,
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                if (resp.isSuccessful) {
                    // Refresh user
                    val refreshed = api.getUser(userId).body()
                    updateState.value = UpdateProfileState.Success(refreshed)
                    onComplete(true, null)
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to update user" }
                    updateState.value = UpdateProfileState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                updateState.value = UpdateProfileState.Error(msg)
                onComplete(false, msg)
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
