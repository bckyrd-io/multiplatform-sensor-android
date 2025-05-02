package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController
) {
    val goals = remember {
        listOf(
            Goal("Daily Steps", 10000, 7500, true),
            Goal("Weekly Distance", 35, 25, false),
            Goal("Monthly Calories", 50000, 35000, true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                },
                actions = {
                    Button(onClick = { /* TODO: Show add goal dialog */ }) {
                        Text("Add Goal")
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
            // Goals List
            LazyColumn {
                items(goals.size) { index ->
                    GoalCard(
                        goal = goals[index],
                        onToggleNotification = { /* TODO: Implement notification toggle */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onToggleNotification: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title and Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${goal.currentValue}/${goal.targetValue}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = goal.progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notification Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notifications")
                Switch(
                    checked = goal.notificationsEnabled,
                    onCheckedChange = { onToggleNotification() }
                )
            }
        }
    }
}

data class Goal(
    val title: String,
    val targetValue: Int,
    val currentValue: Int,
    val notificationsEnabled: Boolean
) {
    val progress: Float
        get() = (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
}
