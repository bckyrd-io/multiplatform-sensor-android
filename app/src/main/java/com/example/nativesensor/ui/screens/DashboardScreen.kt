package com.example.nativesensor.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Test Data
private val todaySummary = mapOf(
    "Steps" to "5,000",
    "Calories" to "250 kcal",
    "Distance" to "3 km"
)

private val quickActions = listOf(
    "Run",
    "Cycle",
    "Weightlifting",
    "Yoga"
)

private val recentWorkouts = listOf(
    "Run" to "2023-05-01",
    "Cycle" to "2023-04-30",
    "Weightlifting" to "2023-04-29"
)

@Composable
fun AccelerometerSummary(): Triple<Float, Float, Float> {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val x = remember { mutableStateOf(0f) }
    val y = remember { mutableStateOf(0f) }
    val z = remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                x.value = event.values[0]
                y.value = event.values[1]
                z.value = event.values[2]
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return Triple(x.value, y.value, z.value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Weekly") }

    val (accelX, accelY, accelZ) = AccelerometerSummary()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                actions = {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { navController.navigate("profile") }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                expanded = false
                                navController.navigate("login") { popUpTo("login") { inclusive = true } }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Period Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PeriodButton(
                        text = "Weekly",
                        isSelected = selectedPeriod == "Weekly",
                        onClick = { selectedPeriod = "Weekly" }
                    )
                    PeriodButton(
                        text = "Monthly",
                        isSelected = selectedPeriod == "Monthly",
                        onClick = { selectedPeriod = "Monthly" }
                    )
                    PeriodButton(
                        text = "All Time",
                        isSelected = selectedPeriod == "All Time",
                        onClick = { selectedPeriod = "All Time" }
                    )
                }
            }

            item {
                // Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val dataPoints = listOf(1000, 1200, 800, 1500, 2000, 1800, 2200)
                    val path = Path()
                    dataPoints.forEachIndexed { index, value ->
                        val x = size.width / (dataPoints.size - 1) * index
                        val y = size.height - (size.height * value / dataPoints.maxOrNull()!!)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = Color(0xFF2196F3), style = Stroke(width = 2.dp.toPx()))
                }
            }

            item {
                // Sensor Realtime
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        todaySummary.forEach { (title, value) ->
                            InsightCard(title = title, value = value)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Accelerometer (X, Y, Z): %.2f, %.2f, %.2f".format(accelX, accelY, accelZ), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                // Quick Actions
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(quickActions) { action ->
                        FilledCard(
                            modifier = Modifier.size(250.dp),
                            onClick = { navController.navigate("logActivity/$action") }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val icon = when (action) {
                                    "Run" -> Icons.AutoMirrored.Filled.DirectionsRun
                                    "Cycle" -> Icons.Default.DirectionsBike
                                    "Weightlifting" -> Icons.Default.FitnessCenter
                                    "Yoga" -> Icons.Default.SelfImprovement
                                    else -> Icons.Default.Help
                                }
                                Icon(imageVector = icon, contentDescription = action)
                                Text(action)
                            }
                        }
                    }
                }
            }

            item {
                // Recent Workouts
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recent Workouts", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        recentWorkouts.forEach { (activity, date) ->
                            Text("$activity on $date", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("history") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View All History")
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun FilledCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(text)
    }
}

@Composable
fun InsightCard(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}