package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

// Test Data
private val todaySummary = mapOf(
    "steps" to 5000,
    "calories" to 250,
    "distance" to 3
)

private val quickActions = listOf(
    "Run",
    "Cycle",
    "Weightlifting"
)

private val recentWorkouts = listOf(
    "Run" to "2023-05-01",
    "Cycle" to "2023-04-30",
    "Weightlifting" to "2023-04-29"
)

private val goals = mapOf(
    "Running" to 10000,
    "Cycling" to 500,
    "Weightlifting" to 5
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Today's Summary
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Today's Summary", style = MaterialTheme.typography.titleMedium)
                    todaySummary.forEach { (metric, value) ->
                        Text("$metric: $value")
                    }
                    Button(
                        onClick = { navController.navigate("statistics") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Statistics")
                    }
                }
            }
        }

        item {
            // Quick Actions
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickActions) { action ->
                    Button(
                        onClick = { 
                            navController.navigate("logActivity")
                        },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Text(action)
                    }
                }
            }
        }

        item {
            // Recent Workouts
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Recent Workouts", style = MaterialTheme.typography.titleMedium)
                    recentWorkouts.forEach { (activity, date) ->
                        Text("$activity - $date")
                    }
                    Button(
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View All History")
                    }
                }
            }
        }

        item {
            // Goals
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Goals", style = MaterialTheme.typography.titleMedium)
                    goals.forEach { (metric, value) ->
                        Text("$metric: $value")
                    }
                    Button(
                        onClick = { navController.navigate("goals") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View All Goals")
                    }
                }
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(navController = NavController(LocalContext.current))
}

//comment