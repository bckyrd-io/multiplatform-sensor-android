package com.example.figcompose.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
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
                            text = "User Metrics Performance",
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
                    val previousSingle = itemsAll.drop(1).firstOrNull()
                    val pdfLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("application/pdf")
                    ) { uri ->
                        if (uri != null) {
                            val latestPerf = itemsAll.firstOrNull()
                            val title = session?.title ?: (session?.id?.let { "Session #$it" } ?: (sessionId?.let { "Session #$it" } ?: "-"))
                            val bytes = buildPlayerReportPdf(
                                sessionTitle = title,
                                playerName = if (playerName.isNotBlank()) playerName else "Player #$playerId",
                                latest = latestPerf,
                                previous = previousSingle,
                                coachNotes = feedback?.notes
                            )
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(bytes)
                            }
                        }
                    }
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
                        text = "Performance Reports Visualisation",
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
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    LegendRow()
                                    VisualProgressChart(latest = latest, previous = previous.firstOrNull())
                                }
                            }
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
                    Button(
                        onClick = {
                            val fileName = "PlayerProgress-" +
                                (if (playerName.isNotBlank()) playerName else "Player$playerId") +
                                "-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date()) + ".pdf"
                            pdfLauncher.launch(fileName)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        enabled = itemsAll.isNotEmpty()
                    ) {
                        Text("Generate & Print Report (PDF)")
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
            Spacer(Modifier.height(4.dp))
            val tiles = listOf(
                "Distance (km)" to (p.distance_meters?.div(1000.0)?.let { String.format("%.2f", it) } ?: "-"),
                "Speed (m/s)" to (p.speed?.let { String.format("%.2f", it) } ?: "-"),
                "Acceleration (m/s²)" to (p.acceleration?.let { String.format("%.2f", it) } ?: "-"),
                "Deceleration (m/s²)" to (p.deceleration?.let { String.format("%.2f", it) } ?: "-"),
                "Cadence (spm)" to (p.cadence_spm?.let { formatNumber(it) } ?: "-"),
                "Heart Rate (bpm)" to (p.heart_rate?.toString() ?: "-")
            )
            tiles.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.getOrNull(0)?.let { (label, value) ->
                        MetricTile(
                            label = label,
                            value = value,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp, horizontal = 6.dp)
                        )
                    }
                    row.getOrNull(1)?.let { (label, value) ->
                        MetricTile(
                            label = label,
                            value = value,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp, horizontal = 6.dp)
                        )
                    } ?: Spacer(Modifier.weight(1f))
                }
            }
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
    fun delta(curr: Double?, old: Double?): Double? {
        if (curr == null || old == null) return null
        return curr - old
    }
    fun oldStr(old: Double?, unit: String, isKm: Boolean = false): String {
        val v = old ?: return "Old: -"
        val text = if (isKm) String.format("%.2f", v / 1000.0) else formatNumber(v)
        return "Old: $text $unit".trim()
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
                text = "Detailed Change vs. Previous Session",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
            Spacer(Modifier.height(8.dp))
            ChangeRow(label = "Distance (km)", left = oldStr(prev.distance_meters, "km", isKm = true), delta = delta(latest.distance_meters, prev.distance_meters), unit = "km")
            ChangeRow(label = "Speed (m/s)", left = oldStr(prev.speed, "m/s"), delta = delta(latest.speed, prev.speed), unit = "m/s")
            ChangeRow(label = "Acceleration (m/s²)", left = oldStr(prev.acceleration, "m/s²"), delta = delta(latest.acceleration, prev.acceleration), unit = "m/s²")
            ChangeRow(label = "Deceleration (m/s²)", left = oldStr(prev.deceleration, "m/s²"), delta = delta(latest.deceleration, prev.deceleration), unit = "m/s²")
            ChangeRow(label = "Cadence (spm)", left = oldStr(prev.cadence_spm, "spm"), delta = delta(latest.cadence_spm, prev.cadence_spm), unit = "spm")
            ChangeRow(label = "Heart Rate (bpm)", left = oldStr(prev.heart_rate?.toDouble(), "bpm"), delta = delta(latest.heart_rate?.toDouble(), prev.heart_rate?.toDouble()), unit = "bpm")
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun LegendRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).background(Color(0xFFDC2626), RoundedCornerShape(3.dp)))
        Text("  Baseline (Previous)", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Spacer(Modifier.width(16.dp))
        Box(Modifier.size(14.dp).background(Color(0xFF16A34A), RoundedCornerShape(3.dp)))
        Text("  Current", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
    }
}

@Composable
private fun VisualProgressChart(
    latest: com.example.figcompose.service.PerformanceDto,
    previous: com.example.figcompose.service.PerformanceDto?
) {
    val categories = listOf(
        "Distance (km)" to Pair(previous?.distance_meters?.div(1000.0) ?: 0.0, latest.distance_meters?.div(1000.0) ?: 0.0),
        "Speed (m/s)" to Pair(previous?.speed ?: 0.0, latest.speed ?: 0.0),
        "Acceleration (m/s²)" to Pair(previous?.acceleration ?: 0.0, latest.acceleration ?: 0.0),
        "Deceleration (m/s²)" to Pair(previous?.deceleration ?: 0.0, latest.deceleration ?: 0.0),
        "Cadence (spm)" to Pair(previous?.cadence_spm ?: 0.0, latest.cadence_spm ?: 0.0),
        "Heart Rate (bpm)" to Pair(previous?.heart_rate?.toDouble() ?: 0.0, latest.heart_rate?.toDouble() ?: 0.0)
    )
    val baselineColor = Color(0xFFDC2626)
    val currentColor = Color(0xFF16A34A)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(top = 8.dp)
    ) {
        val chartPadding = 24.dp.toPx()
        val w = size.width - chartPadding * 2
        val h = size.height - chartPadding * 2
        val origin = Offset(chartPadding, chartPadding)
        val groupCount = categories.size
        val groupWidth = w / groupCount
        val barWidth = groupWidth * 0.34f
        val gap = groupWidth * 0.06f

        categories.forEachIndexed { index, entry ->
            val base = entry.second.first.toFloat()
            val curr = entry.second.second.toFloat()
            val localMax = maxOf(1f, maxOf(base, curr))
            val gx = origin.x + groupWidth * index

            val baseHeight = (base / localMax) * h
            val currHeight = (curr / localMax) * h

            val baseLeft = gx + gap
            val currLeft = gx + gap + barWidth + gap

            drawRect(
                color = baselineColor,
                topLeft = Offset(baseLeft, origin.y + h - baseHeight),
                size = Size(barWidth, baseHeight)
            )
            drawRect(
                color = currentColor,
                topLeft = Offset(currLeft, origin.y + h - currHeight),
                size = Size(barWidth, currHeight)
            )
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        val labels = categories.map { (name, _) ->
            when {
                name.startsWith("Distance") -> "Distance"
                name.startsWith("Speed") -> "Speed"
                name.startsWith("Acceleration") -> "Accel"
                name.startsWith("Deceleration") -> "Decel"
                name.startsWith("Cadence") -> "Cadence"
                name.startsWith("Heart") -> "HR"
                else -> name
            }
        }
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChangeRow(label: String, left: String, delta: Double?, unit: String) {
    val color = when {
        delta == null -> TextSecondary
        delta > 0 -> Color(0xFF16A34A)
        delta < 0 -> Color(0xFFDC2626)
        else -> TextSecondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
            Text(left, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        }
        Text(
            text = delta?.let { (if (it > 0) "+" else if (it < 0) "" else "±") + String.format("%.2f", it) + " " + unit } ?: "-",
            style = MaterialTheme.typography.titleMedium.copy(color = color)
        )
    }
}

private fun formatNumber(n: Double): String {
    return if (abs(n) >= 100) String.format("%.0f", n) else String.format("%.2f", n)
}

private fun buildPlayerReportPdf(
    sessionTitle: String,
    playerName: String,
    latest: com.example.figcompose.service.PerformanceDto?,
    previous: com.example.figcompose.service.PerformanceDto?,
    coachNotes: String?
): ByteArray {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = doc.startPage(pageInfo)
    val c = page.canvas
    val titlePaint = Paint().apply { textSize = 18f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val subPaint = Paint().apply { textSize = 12f; isAntiAlias = true }
    val labelPaint = Paint().apply { textSize = 12f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val valuePaint = Paint().apply { textSize = 12f; isAntiAlias = true }

    var y = 40f
    c.drawText("Player Progress Report", 40f, y, titlePaint)
    y += 22f
    c.drawText("Session: $sessionTitle", 40f, y, subPaint)
    y += 18f
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    c.drawText("Player: $playerName    Generated: $dateStr", 40f, y, subPaint)
    y += 28f

    c.drawText("Latest Metrics", 40f, y, labelPaint)
    y += 18f
    fun row(lbl: String, v: String) { c.drawText(lbl, 40f, y, subPaint); c.drawText(v, 260f, y, valuePaint); y += 16f }
    val dist = latest?.distance_meters?.div(1000.0)?.let { String.format("%.2f km", it) } ?: "-"
    val spd = latest?.speed?.let { String.format("%.2f m/s", it) } ?: "-"
    val acc = latest?.acceleration?.let { String.format("%.2f m/s²", it) } ?: "-"
    val dec = latest?.deceleration?.let { String.format("%.2f m/s²", it) } ?: "-"
    val cad = latest?.cadence_spm?.let { formatNumber(it) + " spm" } ?: "-"
    val hr = latest?.heart_rate?.let { "$it bpm" } ?: "-"
    row("Distance", dist)
    row("Speed", spd)
    row("Acceleration", acc)
    row("Deceleration", dec)
    row("Cadence", cad)
    row("Heart Rate", hr)

    y += 10f
    c.drawText("Visual Progress Comparison", 40f, y, labelPaint)
    y += 10f
    val chartLeft = 40f
    val chartTop = y
    val chartWidth = 515f
    val chartHeight = 140f
    val cat = listOf(
        Pair(previous?.distance_meters?.div(1000.0) ?: 0.0, latest?.distance_meters?.div(1000.0) ?: 0.0),
        Pair(previous?.speed ?: 0.0, latest?.speed ?: 0.0),
        Pair(previous?.acceleration ?: 0.0, latest?.acceleration ?: 0.0),
        Pair(previous?.deceleration ?: 0.0, latest?.deceleration ?: 0.0),
        Pair(previous?.cadence_spm ?: 0.0, latest?.cadence_spm ?: 0.0),
        Pair(previous?.heart_rate?.toDouble() ?: 0.0, latest?.heart_rate?.toDouble() ?: 0.0)
    )
    val maxVal = cat.flatMap { listOf(it.first, it.second) }.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val baselinePaint = Paint().apply { color = 0xFF1D4ED8.toInt() }
    val currentPaint = Paint().apply { color = 0xFFF59E0B.toInt() }
    val groups = cat.size
    val groupWidth = chartWidth / groups
    val barWidth = groupWidth * 0.34f
    val gap = groupWidth * 0.06f
    for (i in 0 until groups) {
        val base = cat[i].first.toFloat()
        val cur = cat[i].second.toFloat()
        val baseH = ((base / maxVal).toFloat()) * chartHeight
        val curH = ((cur / maxVal).toFloat()) * chartHeight
        val gx = chartLeft + groupWidth * i
        val baseLeft = gx + gap
        val curLeft = gx + gap + barWidth + gap
        c.drawRect(baseLeft, chartTop + chartHeight - baseH, baseLeft + barWidth, chartTop + chartHeight, baselinePaint)
        c.drawRect(curLeft, chartTop + chartHeight - curH, curLeft + barWidth, chartTop + chartHeight, currentPaint)
    }
    y = chartTop + chartHeight + 18f

    coachNotes?.let {
        c.drawText("Coach Observations", 40f, y, labelPaint)
        y += 18f
        val textPaint = subPaint
        val words = it.split(" ")
        var line = ""
        for (w in words) {
            val test = if (line.isEmpty()) w else "$line $w"
            if (textPaint.measureText(test) > 515f) {
                c.drawText(line, 40f, y, textPaint)
                y += 16f
                line = w
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) {
            c.drawText(line, 40f, y, textPaint)
            y += 16f
        }
    }

    doc.finishPage(page)
    val bos = ByteArrayOutputStream()
    doc.writeTo(bos)
    doc.close()
    return bos.toByteArray()
}

@Preview(showBackground = true)
@Composable
fun PlayerProgressPreview() {
    FigcomposeTheme {
        PlayerProgressScreen(playerId = 1, playerName = "Alex Johnson", sessionId = null)
    }
}
