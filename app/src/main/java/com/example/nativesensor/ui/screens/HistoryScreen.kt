package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController
) {
    val workouts = remember {
        listOf(
            Workout("Running", "2025-05-02", 30, 5.0, "Moderate"),
            Workout("Cycling", "2025-05-01", 45, 10.0, "High"),
            Workout("Weightlifting", "2025-05-01", 60, 0.0, "High")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Date Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today")
                Text("This Week")
                Text("This Month")
                Text("All")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workouts List
            LazyColumn {
                items(workouts.size) { index ->
                    WorkoutCard(workouts[index])
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(workout: Workout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = workout.activityType,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = workout.date,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Duration: ${workout.duration} min")
                Text("Distance: ${"%.1f".format(workout.distance)} km")
            }
            Text("Intensity: ${workout.intensity}")
        }
    }
}

data class Workout(
    val activityType: String,
    val date: String,
    val duration: Int,
    val distance: Double,
    val intensity: String
)
