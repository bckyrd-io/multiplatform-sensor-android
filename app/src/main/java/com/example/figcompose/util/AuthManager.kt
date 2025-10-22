package com.example.figcompose.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.figcompose.service.ApiService
import com.example.figcompose.service.RetrofitProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * Authentication state sealed class to represent different authentication states
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: Map<String, Any?>) : AuthState()
    data class Unauthenticated(val message: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * AuthManager handles all authentication-related operations
 */
class AuthManager(private val context: Context) : ViewModel() {
    private val apiService: ApiService = RetrofitProvider.api()
    private val prefs: SharedPreferences = context.getSharedPreferences("figcompose_auth", Context.MODE_PRIVATE)
    
    // Current authentication state
    val authState = mutableStateOf<AuthState>(AuthState.Idle)
    
    // Current user data (null if not logged in)
    val currentUser = mutableStateOf<Map<String, Any?>?>(null)
    
    /**
     * Check if user is logged in from SharedPreferences
     */
    fun checkAuthState() {
        val userId = prefs.getLong("user_id", -1L)
        val username = prefs.getString("username", null)
        val email = prefs.getString("email", null)
        val role = prefs.getString("role", null)
        
        if (userId != -1L && username != null && email != null && role != null) {
            currentUser.value = mutableMapOf(
                "id" to userId,
                "username" to username,
                "email" to email,
                "role" to role,
                "fullName" to prefs.getString("full_name", null),
                "phone" to prefs.getString("phone", null)
            )
            authState.value = AuthState.Authenticated(currentUser.value!!)
        } else {
            authState.value = AuthState.Unauthenticated()
        }
    }
    
    /**
     * Login with email/username and password
     */
    fun login(identifier: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authState.value = AuthState.Loading
                val response = apiService.login(username = identifier, password = password)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val userJson = response.body()?.user
                    if (userJson != null) {
                        // Save user data to SharedPreferences
                        with(prefs.edit()) {
                            putLong("user_id", userJson.getLong("id"))
                            putString("username", userJson.getString("username"))
                            putString("email", userJson.getString("email"))
                            putString("role", userJson.getString("role"))
                            putString("full_name", userJson.getString("fullName"))
                            putString("phone", userJson.getString("phone"))
                            apply()
                        }
                        
                        // Update current user
                        currentUser.value = userJson.toMutableMap()
                        authState.value = AuthState.Authenticated(userJson)
                        onComplete(true, null)
                    } else {
                        authState.value = AuthState.Error("Invalid response from server")
                        onComplete(false, "Invalid response from server")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string()?.let {
                        try {
                            JSONObject(it).getString("error")
                        } catch (e: Exception) {
                            "Invalid credentials"
                        }
                    } ?: "Login failed. Please try again."
                    
                    authState.value = AuthState.Error(errorMessage)
                    onComplete(false, errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                authState.value = AuthState.Error(errorMessage)
                onComplete(false, errorMessage)
            }
        }
    }
    
    /**
     * Register a new user
     */
    fun register(
        username: String,
        email: String,
        password: String,
        fullName: String? = null,
        phone: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                authState.value = AuthState.Loading
                
                val response = apiService.register(
                    username = username,
                    email = email,
                    password = password,
                    fullName = fullName,
                    phone = phone
                )
                
                if (response.isSuccessful) {
                    login(username, password, onComplete)
                } else {
                    val errorMessage = response.errorBody()?.string()?.let {
                        try {
                            JSONObject(it).getString("error")
                        } catch (e: Exception) {
                            "Registration failed"
                        }
                    } ?: "Registration failed. Please try again."
                    
                    authState.value = AuthState.Error(errorMessage)
                    onComplete(false, errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                authState.value = AuthState.Error(errorMessage)
                onComplete(false, errorMessage)
            }
        }
    }
    
    /**
     * Update user profile
     */
    fun updateProfile(
        userId: Long,
        username: String? = null,
        email: String? = null,
        fullName: String? = null,
        phone: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                authState.value = AuthState.Loading
                
                // Get current user data
                val currentUser = currentUser.value ?: run {
                    authState.value = AuthState.Error("User not logged in")
                    onComplete(false, "User not logged in")
                    return@launch
                }
                
                // Update user data
                val updatedUser = currentUser.toMutableMap().apply {
                    username?.let { put("username", it) }
                    email?.let { put("email", it) }
                    fullName?.let { put("fullName", it) }
                    phone?.let { put("phone", it) }
                }
                
                // Call API to update user
                val response = apiService.updateUser(
                    userId = userId,
                    username = updatedUser.getString("username"),
                    email = updatedUser.getString("email"),
                    fullName = updatedUser.getString("fullName"),
                    phone = updatedUser.getString("phone")
                )
                
                if (response.isSuccessful) {
                    // Update local user data with server response
                    response.body()?.let { serverUser ->
                        this@AuthManager.currentUser.value = serverUser.toMutableMap()
                        
                        // Update SharedPreferences
                        with(prefs.edit()) {
                            putString("username", serverUser.getString("username"))
                            putString("email", serverUser.getString("email"))
                            putString("full_name", serverUser.getString("fullName"))
                            putString("phone", serverUser.getString("phone"))
                            apply()
                        }
                    }
                    
                    authState.value = AuthState.Authenticated(updatedUser)
                    onComplete(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()?.let {
                        try {
                            JSONObject(it).getString("error")
                        } catch (e: Exception) {
                            "Failed to update profile"
                        }
                    } ?: "Failed to update profile. Please try again."
                    
                    authState.value = AuthState.Error(errorMessage)
                    onComplete(false, errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                authState.value = AuthState.Error(errorMessage)
                onComplete(false, errorMessage)
            }
        }
    }
    
    /**
     * Change user password
     */
    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                authState.value = AuthState.Loading
                
                // First verify current password
                val currentUser = currentUser.value ?: run {
                    authState.value = AuthState.Error("User not logged in")
                    onComplete(false, "User not logged in")
                    return@launch
                }
                
                // In a real app, you would verify the current password with the server
                // For now, we'll just check if the user is authenticated
                
                // Call API to update password
                // Note: This is a placeholder - you'll need to implement the actual API call
                // val response = apiService.changePassword(userId, currentPassword, newPassword)
                
                // For now, we'll simulate a successful response
                // In a real implementation, you would handle the actual API response
                withContext(Dispatchers.IO) {
                    // Simulate network delay
                    Thread.sleep(1000)
                }
                
                // If we get here, assume the password change was successful
                onComplete(true, null)
                
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                authState.value = AuthState.Error(errorMessage)
                onComplete(false, errorMessage)
            }
        }
    }
    
    /**
     * Logout the current user
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Clear user data from SharedPreferences
                with(prefs.edit()) {
                    clear()
                    apply()
                }
                
                // Reset current user and auth state
                currentUser.value = null
                authState.value = AuthState.Unauthenticated()
                
                // Call API to invalidate session (if applicable)
                // apiService.logout()
                
            } catch (e: Exception) {
                // Even if logout fails, we still want to clear local data
                with(prefs.edit()) {
                    clear()
                    apply()
                }
                currentUser.value = null
                authState.value = AuthState.Unauthenticated()
            }
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        return currentUser.value != null && authState.value is AuthState.Authenticated
    }
    
    /**
     * Get authentication token (if using token-based auth)
     */
    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }
    
    /**
     * Clear all user data (for account deletion)
     */
    fun clearUserData() {
        with(prefs.edit()) {
            clear()
            apply()
        }
        currentUser.value = null
        authState.value = AuthState.Unauthenticated()
    }
}

// Extension functions for Map<String, Any?> to safely access user properties
private fun Map<String, Any?>.getLong(key: String, default: Long = 0L): Long {
    return (this[key] as? Number)?.toLong() ?: default
}

private fun Map<String, Any?>.getString(key: String): String? {
    return this[key] as? String
}

private fun Map<String, Any?>.getStringOrEmpty(key: String): String {
    return getString(key) ?: ""
}

private fun Map<String, Any?>.getInt(key: String, default: Int = 0): Int {
    return (this[key] as? Number)?.toInt() ?: default
}

private fun Map<String, Any?>.getBoolean(key: String, default: Boolean = false): Boolean {
    return this[key] as? Boolean ?: default
}
