package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.figcompose.ui.theme.*
import com.example.figcompose.util.SessionManager
import com.example.figcompose.util.ReportState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProgressScreen(
    playerId: Int,
    playerName: String = "",
    sessionId: Int? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val reportState by sessionManager.reportState

    LaunchedEffect(sessionId) {
        sessionId?.let { sessionManager.loadReport(it) }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Player Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (val rs = reportState) {
                is ReportState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }
                is ReportState.Error -> {
                    Text(
                        text = rs.message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ReportState.Loaded -> {
                    val report = rs.report
                    val session = report.session
                    // Header
                    Text(
                        text = "Session: ${session?.title ?: (session?.id?.let { "Session #$it" } ?: (sessionId?.let { "Session #$it" } ?: "-"))}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        ),
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                    val timeMeta = listOfNotNull(session?.start_time, session?.end_time).joinToString(" · ")
                    if (timeMeta.isNotBlank()) {
                        Text(
                            text = timeMeta,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp)
                        )
                    }

                    Text(
                        text = "Performance Metrics (${playerName})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )

                    val perf = report.performances.filter { it.player_id == playerId && (sessionId == null || it.session_id == sessionId) }
                    MetricsCard(perf)
                }
                else -> {
                    if (sessionId == null) {
                        Text(
                            text = "No session selected.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsCard(perf: List<com.example.figcompose.service.PerformanceDto>) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            val avgHr = perf.mapNotNull { it.heart_rate?.toDouble() }.average().takeIf { !it.isNaN() }
            val maxHr = perf.mapNotNull { it.heart_rate }.maxOrNull()
            val totalDistance = perf.mapNotNull { it.distance_meters }.sum()
            val avgSpeed = perf.mapNotNull { it.speed }.average().takeIf { !it.isNaN() }

            SectionTitle("Heart Rate")
            KeyValueRow("Average", avgHr?.let { "${"%.0f".format(it)} bpm" } ?: "-")
            KeyValueRow("Max", maxHr?.let { "$it bpm" } ?: "-")
            // Trend requires historical context; placeholder omitted

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E7EB))

            SectionTitle("Distance Run")
            KeyValueRow("Total", if (totalDistance > 0) String.format("%.2f km", totalDistance / 1000.0) else "-")

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E7EB))

            SectionTitle("Speed (m/s)")
            KeyValueRow("Average", avgSpeed?.let { String.format("%.2f m/s", it) } ?: "-")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    )
}

@Composable
private fun KeyValueRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        Text(
            text = value,
            style = if (highlight) {
                MaterialTheme.typography.titleMedium.copy(color = BluePrimary, fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleMedium.copy(color = BluePrimary)
            }
        )
    }
}

@Composable
private fun TrendRow(text: String, up: Boolean) {
    val color = if (up) Color(0xFF16A34A) else Color(0xFFDC2626)
    val arrow = if (up) "↑" else "↓"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$arrow  $text",
            color = color,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerProgressPreview() {
    FigcomposeTheme {
        PlayerProgressScreen(playerId = 1, playerName = "Alex Johnson", sessionId = null)
    }
}
