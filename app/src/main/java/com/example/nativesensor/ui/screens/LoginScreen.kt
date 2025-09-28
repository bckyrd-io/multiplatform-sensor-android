package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.ui.Alignment

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.example.nativesensor.network.ApiClient
import com.example.nativesensor.network.LoginRequest
import com.example.nativesensor.network.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginResult by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = { /* TODO: Implement forgot password */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?")
        }

        Button(
            onClick = {
                isLoggingIn = true
                loginResult = ""
                val request = LoginRequest(email, password)
                ApiClient.apiService.loginUser(request).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        isLoggingIn = false
                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            loginResult = loginResponse.message
                            if (loginResponse.success) {
                                // Store user information
                                loginResponse.user?.let { user ->
                                    UserSession.currentUser = user
                                    UserSession.userId = user.id.toString()
                                    UserSession.userRole = user.role
                                }
                                
                                // Navigate based on role
                                if (loginResponse.user?.role == "admin") {
                                    navController.navigate("adminDashboard")
                                } else {
                                    navController.navigate("dashboard")
                                }
                            }
                        } else {
                            loginResult = "Login failed: ${response.message()}"
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        isLoggingIn = false
                        loginResult = "Error: ${t.localizedMessage}"
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoggingIn
        ) {
            Text(if (isLoggingIn) "Logging in..." else "Login")
        }

        if (loginResult.isNotEmpty()) {
            Text(
                text = loginResult,
                color = if (loginResult.contains("success", true)) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// User session object to store current user information
object UserSession {
    var currentUser: com.example.nativesensor.network.UserData? = null
    var userId: String = ""
    var userRole: String = "user"
    
    fun isAdmin(): Boolean = userRole == "admin"
    fun clear() {
        currentUser = null
        userId = ""
        userRole = "user"
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavController(LocalContext.current))
}
