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
    var goals by remember {
        mutableStateOf(listOf(
            Goal("Daily Steps", 10000, 7500, true),
            Goal("Weekly Distance", 35, 25, false),
            Goal("Monthly Calories", 50000, 35000, true)
        ))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
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
                items(goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onToggleNotification = { 
                            goals = goals.map { g ->
                                if (g.title == goal.title) g.copy(notificationsEnabled = !g.notificationsEnabled) 
                                else g
                            }
                        },
                        onTargetValueChanged = { newValue ->
                            goals = goals.map { g ->
                                if (g.title == goal.title) g.copy(targetValue = newValue) 
                                else g
                            }
                        },
                        onCurrentValueChanged = { newValue ->
                            goals = goals.map { g ->
                                if (g.title == goal.title) g.copy(currentValue = newValue) 
                                else g
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Save Button
            Button(
                onClick = {
                    // TODO: Save goals data
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Goals")
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onToggleNotification: () -> Unit,
    onTargetValueChanged: (Int) -> Unit,
    onCurrentValueChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Value Slider
            Text("Target Value")
            Slider(
                value = goal.targetValue.toFloat(),
                onValueChange = { 
                    onTargetValueChanged(it.toInt())
                },
                valueRange = 0f..100000f,
                steps = 999,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Current: ${goal.targetValue}")

            Spacer(modifier = Modifier.height(16.dp))

            // Current Value Slider
            Text("Current Value")
            Slider(
                value = goal.currentValue.toFloat(),
                onValueChange = { 
                    onCurrentValueChanged(it.toInt())
                },
                valueRange = 0f..goal.targetValue.toFloat(),
                steps = goal.targetValue - 1,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Current: ${goal.currentValue}")

            Spacer(modifier = Modifier.height(16.dp))

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
    var targetValue: Int,
    var currentValue: Int,
    var notificationsEnabled: Boolean
) {
    val progress: Float
        get() = (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
}
