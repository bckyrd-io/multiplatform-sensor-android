package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogActivityScreen(
    navController: NavController,
    onActivityLogged: () -> Unit = {}
) {
    var activityType by remember { mutableStateOf("Running") }
    var duration by remember { mutableStateOf(30) }
    var distance by remember { mutableStateOf(0) }
    var intensity by remember { mutableStateOf("Moderate") }
    var isGpsEnabled by remember { mutableStateOf(true) }
    var isStepTrackingEnabled by remember { mutableStateOf(true) }
    var isMotionTrackingEnabled by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {

            TopAppBar(
                title = { Text("log Activity") },
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
            // Activity Type Selection
            OutlinedTextField(
                value = activityType,
                onValueChange = { activityType = it },
                label = { Text("Activity Type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Duration Input
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = {
                    val value = it.toIntOrNull() ?: 0
                    duration = value.coerceIn(0..120)
                },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Distance Input
            OutlinedTextField(
                value = distance.toString(),
                onValueChange = {
                    val value = it.toIntOrNull() ?: 0
                    distance = value.coerceIn(0..50)
                },
                label = { Text("Distance (km)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

        

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    // TODO: Implement activity logging
                    onActivityLogged()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Activity")
            }
        }
    }
}
