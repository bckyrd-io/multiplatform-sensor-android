package com.example.figcompose.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.figcompose.ui.theme.*
import com.example.figcompose.util.DashboardManager
import com.example.figcompose.util.DashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSettings: () -> Unit = {},
    onAdd: () -> Unit = {},
    onSessionClick: (Int) -> Unit = {},
    onUsersClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val dashboardManager = remember { DashboardManager(context) }
    val state by dashboardManager.state

    LaunchedEffect(Unit) {
        dashboardManager.loadOverview()
    }

    val sessions = (state as? DashboardState.Loaded)?.sessions.orEmpty()
    val users = (state as? DashboardState.Loaded)?.users.orEmpty()
    val pps = (state as? DashboardState.Loaded)?.playersPerSession.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Settings",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onAdd) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                                tint = Color.White
                            )
                        }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                PerformanceCard(
                    data = pps,
                    isLoading = state is DashboardState.Loading
                )
                Spacer(Modifier.height(16.dp))
                // Overview header
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(title = "Sessions", value = sessions.size.toString(), modifier = Modifier.weight(1f))
                    StatCard(title = "Profiles", value = users.size.toString(), modifier = Modifier.weight(1f), onClick = onUsersClick)
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Active Sessions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (state is DashboardState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }
            }

            if (sessions.isEmpty() && state is DashboardState.Loaded) {
                item {
                    Text(
                        text = "No sessions found",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(sessions) { s ->
                    val title = s.title ?: "Session #${s.id}"
                    val subtitle = listOfNotNull(s.session_type, s.location).joinToString(" • ")
                    SessionItem(
                        title = title,
                        subtitle = if (subtitle.isNotBlank()) subtitle else (s.description ?: ""),
                        onClick = { onSessionClick(s.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else {
            modifier
        },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun PerformanceCard(
    data: List<com.example.figcompose.service.PlayersPerSessionDto>,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Players per session",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            if (isLoading) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            setPinchZoom(true)
                            axisRight.isEnabled = false
                            legend.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(true)
                            axisLeft.setDrawGridLines(true)
                        }
                    },
                    update = { chart ->
                        val items = if (data.isEmpty()) emptyList() else data.take(10).asReversed()
                        if (items.isEmpty()) {
                            chart.data = null
                            chart.invalidate()
                            return@AndroidView
                        }

                        val entries = items.mapIndexed { index, item ->
                            Entry(index.toFloat(), item.players_count.toFloat())
                        }
                        // Session labels: short title or fallback to S#ID
                        val labels = items.map { s ->
                            s.session_title?.take(12)?.ifBlank { "S#${s.session_id}" } ?: "S#${s.session_id}"
                        }

                        val color = BluePrimary.toArgb()
                        val set = LineDataSet(entries, "Players").apply {
                            setDrawFilled(true)
                            setDrawValues(false)
                            setColor(color)
                            setCircleColor(color)
                            lineWidth = 2.5f
                            circleRadius = 3.5f
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            fillColor = color
                            fillAlpha = 60
                        }

                        chart.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(labels)
                            granularity = 1f
                            setLabelCount(labels.size, true)
                            labelRotationAngle = -25f
                        }
                        chart.axisLeft.axisMinimum = 0f
                        chart.data = LineData(set)
                        chart.invalidate()
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            if (data.isEmpty() && !isLoading) {
                Text(
                    text = "No attendance yet",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun SessionItem(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEFF6FF)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDCEFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    tint = BluePrimary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = "Go",
                tint = BluePrimary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DashboardScreenPreview() {
    FigcomposeTheme {
        DashboardScreen()
    }
}
