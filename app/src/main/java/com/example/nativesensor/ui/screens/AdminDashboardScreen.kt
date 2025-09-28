package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nativesensor.network.ApiClient
import com.example.nativesensor.network.UsersResponse
import com.example.nativesensor.network.UserStatsResponse
import com.example.nativesensor.model.UserStats
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    LaunchedEffect(selectedUserId) {
        selectedUserId?.let { userId ->
            loadUserStats(userId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            // Users List
            Text(
                text = "Users",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        isSelected = selectedUserId == (user["id"] as? Number)?.toInt(),
                        onClick = {
                            selectedUserId = (user["id"] as? Number)?.toInt()
                        }
                    )
                }
            }

            // User Stats
            selectedUserId?.let { userId ->
                userStats?.let { stats ->
                    UserStatsCard(stats = stats)
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("dashboard") }
            ) {
                Text("Back to Dashboard")
            }
            
            Button(
                onClick = { 
                    UserSession.clear()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text("Logout")
            }
        }
    }

    fun loadUsers() {
        isLoading = true
        error = ""
        
        ApiClient.apiService.getAllUsers().enqueue(object : Callback<UsersResponse> {
            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    users = response.body()!!.users
                } else {
                    error = "Failed to load users: ${response.message()}"
                }
            }
            
            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                isLoading = false
                error = "Error: ${t.localizedMessage}"
            }
        })
    }

    fun loadUserStats(userId: Int) {
        ApiClient.apiService.getUserStatsForUser(
            userRole = "admin",
            currentUserId = userId.toString()
        ).enqueue(object : Callback<UserStatsResponse> {
            override fun onResponse(call: Call<UserStatsResponse>, response: Response<UserStatsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    userStats = UserStats.fromMap(responseBody.totalStats, responseBody.todayStats)
                }
            }
            
            override fun onFailure(call: Call<UserStatsResponse>, t: Throwable) {
                // Handle error silently for stats
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    user: Map<String, Any>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                           else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user["name"] as? String ?: "Unknown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user["email"] as? String ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Role: ${user["role"] as? String ?: "user"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun UserStatsCard(stats: UserStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "User Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Total Steps", stats.totalSteps.toString())
                StatItem("Total Calories", stats.totalCalories.toString())
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Avg Heart Rate", "${stats.avgHeartRate.toInt()} BPM")
                StatItem("Today Steps", stats.todaySteps.toString())
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Today Calories", stats.todayCalories.toString())
                StatItem("Records", stats.totalRecords.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
} 