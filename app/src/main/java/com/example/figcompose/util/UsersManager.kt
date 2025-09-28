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

sealed class UsersState {
    object Idle : UsersState()
    object Loading : UsersState()
    data class Loaded(val users: List<UserDto>) : UsersState()
    data class Error(val message: String) : UsersState()
}

class UsersManager(private val context: Context) : ViewModel() {
    private val api: ApiService = RetrofitProvider.api()

    val state = mutableStateOf<UsersState>(UsersState.Idle)

    fun loadUsers(q: String? = null, limit: Int? = 200, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                state.value = UsersState.Loading
                val resp = api.getUsers(q = q, limit = limit)
                if (resp.isSuccessful) {
                    val users = resp.body().orElseEmpty()
                    state.value = UsersState.Loaded(users)
                    onComplete(true, null)
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to load users" }
                    state.value = UsersState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                state.value = UsersState.Error(msg)
                onComplete(false, msg)
            }
        }
    }

    private fun <T> List<T>?.orElseEmpty(): List<T> = this ?: emptyList()

    private fun parseErrorBody(body: String?): String {
        return try {
            if (body.isNullOrBlank()) return ""
            JSONObject(body).optString("error").ifBlank { body }
        } catch (ex: Exception) {
            body ?: ""
        }
    }
}
