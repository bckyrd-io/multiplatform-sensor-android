package com.example.nativesensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController
) {
    var selectedPeriod by remember { mutableStateOf("Weekly") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                val padding = 20f
                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2
                
                // Draw grid lines
                drawLine(
                    Color.LightGray,
                    Offset(padding, padding),
                    Offset(padding, height - padding),
                    strokeWidth = 1f
                )
                drawLine(
                    Color.LightGray,
                    Offset(padding, height - padding),
                    Offset(width - padding, height - padding),
                    strokeWidth = 1f
                )
                
                // Draw data points
                val dataPoints = listOf(
                    1000, 1200, 1400, 1600, 1800, 2000, 2200
                )
                
                val path = Path()
                dataPoints.forEachIndexed { index, value ->
                    val x = padding + (chartWidth * index / (dataPoints.size - 1).toFloat())
                    val y = height - padding - (chartHeight * value.toFloat() / 2200f)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                // Draw line
                drawPath(
                    path = path,
                    color = Color(0xFF4CAF50), // Green color
                    style = Stroke(width = 2f)
                )
                
                // Draw points
                dataPoints.forEachIndexed { index, value ->
                    val x = padding + (chartWidth * index / (dataPoints.size - 1).toFloat())
                    val y = height - padding - (chartHeight * value.toFloat() / 2200f)
                    drawCircle(
                        color = Color(0xFF4CAF50), // Green color
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Insights Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InsightCard(
                    title = "Average Steps",
                    value = "8,500"
                )
                InsightCard(
                    title = "Total Distance",
                    value = "35 km"
                )
                InsightCard(
                    title = "Calories Burned",
                    value = "2,500"
                )
            }
        }
    }
}

@Composable
private fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
