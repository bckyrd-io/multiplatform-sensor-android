package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.example.figcompose.service.RetrofitProvider
import com.example.figcompose.ui.theme.*
import com.example.figcompose.util.ReportState
import com.example.figcompose.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProgressScreen(
    playerId: Int,
    playerName: String = "",
    sessionId: Int? = null,
    onBack: () -> Unit = {},
    canAddFeedback: Boolean = false,
    onAddFeedback: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val reportState by sessionManager.reportState
    val api = remember(context) { RetrofitProvider.api() }
    var history by remember { mutableStateOf<List<com.example.figcompose.service.PerformanceDto>?>(null) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionId) {
        sessionId?.let { sessionManager.loadReport(it) }
    }
    LaunchedEffect(playerId) {
        historyLoading = true
        historyError = null
        try {
            val resp = api.getPlayerPerformance(playerId)
            if (resp.isSuccessful) {
                history = resp.body().orEmpty()
            } else {
                historyError = "Failed to load performance history"
            }
        } catch (e: Exception) {
            historyError = e.message
        } finally {
            historyLoading = false
        }
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
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Home")
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
                .verticalScroll(rememberScrollState())
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Performance Metrics (${playerName})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }

                    val feedback = report.feedback?.firstOrNull {
                        it.player_id == playerId &&
                        (sessionId == null || it.session_id == sessionId)
                    }
                    val itemsAll = history.orEmpty().sortedWith(compareByDescending<com.example.figcompose.service.PerformanceDto> { it.recorded_at ?: "" }.thenByDescending { it.id })
                    if (itemsAll.isNotEmpty()) {
                        MetricsCardLatest(itemsAll.first())
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No performance data available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Performance Report",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    when {
                        historyLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = BluePrimary) }
                        }
                        historyError != null -> {
                            Text(
                                text = historyError ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        !history.isNullOrEmpty() -> {
                            val items = history!!.sortedWith(compareByDescending<com.example.figcompose.service.PerformanceDto> { it.recorded_at ?: "" }.thenByDescending { it.id })
                            val latest = items.first()
                            val previous = items.drop(1).take(7)
                            PerformanceSummarySection(latest, previous)
                        }
                    }

                    if (feedback != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Coach Feedback",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Feedback from: ${feedback.coach_username ?: "Coach"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = feedback.notes ?: "No notes provided",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (canAddFeedback && feedback == null) {
                        OutlinedButton(
                            onClick = onAddFeedback,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BluePrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text("Add Feedback")
                        }
                    }
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

@Composable
private fun MetricsCardLatest(p: com.example.figcompose.service.PerformanceDto) {
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
            SectionTitle("Latest Metrics")
            KeyValueRow("Distance", p.distance_meters?.let { String.format("%.2f km", it / 1000.0) } ?: "-")
            KeyValueRow("Speed", p.speed?.let { String.format("%.2f m/s", it) } ?: "-")
            KeyValueRow("Acceleration", p.acceleration?.let { String.format("%.2f m/s²", it) } ?: "-")
            KeyValueRow("Deceleration", p.deceleration?.let { String.format("%.2f m/s²", it) } ?: "-")
            KeyValueRow("Cadence", p.cadence_spm?.let { "$it spm" } ?: "-")
            KeyValueRow("Heart Rate", p.heart_rate?.let { "$it bpm" } ?: "-")
        }
    }
}

@Composable
private fun PerformanceSummarySection(
    latest: com.example.figcompose.service.PerformanceDto,
    previous: List<com.example.figcompose.service.PerformanceDto>
) {
    if (previous.isEmpty()) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = "Not enough history to summarize",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
        return
    }

    val prev = previous.first()
    fun deltaStr(curr: Double?, old: Double?, unit: String): String {
        if (curr == null || old == null) return "-"
        val d = curr - old
        val sign = if (d > 0) "+" else if (d < 0) "" else "±"
        return "$sign${String.format("%.2f", d)} $unit"
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Change vs previous",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Distance", deltaStr(latest.distance_meters, prev.distance_meters, "m"))
            KeyValueRow("Speed", deltaStr(latest.speed, prev.speed, "m/s"))
            KeyValueRow("Acceleration", deltaStr(latest.acceleration, prev.acceleration, "m/s²"))
            KeyValueRow("Deceleration", deltaStr(latest.deceleration, prev.deceleration, "m/s²"))
            KeyValueRow("Cadence", deltaStr(latest.cadence_spm?.toDouble(), prev.cadence_spm?.toDouble(), "spm"))
            KeyValueRow("Heart Rate", deltaStr(latest.heart_rate?.toDouble(), prev.heart_rate?.toDouble(), "bpm"))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerProgressPreview() {
    FigcomposeTheme {
        PlayerProgressScreen(playerId = 1, playerName = "Alex Johnson", sessionId = null)
    }
}
