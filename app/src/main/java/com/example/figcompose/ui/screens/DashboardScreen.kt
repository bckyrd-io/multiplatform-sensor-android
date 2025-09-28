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
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                            imageVector = Icons.Outlined.Settings,
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
                    StatCard(title = "Users", value = users.size.toString(), modifier = Modifier.weight(1f), onClick = onUsersClick)
                }
                Spacer(Modifier.height(16.dp))
                PerformanceCard()
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
private fun PerformanceCard() {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(BluePrimary)
                Text(
                    text = "  Player  ",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary)
                )
                LegendDot(Color(0xFFD1D5DB))
                Text(
                    text = "  Session",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                // Simple grid + line illustration
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)) {
                    val w = size.width
                    val h = size.height
                    val gridColor = Color(0xFFE5E7EB)
                    // vertical grid
                    val cols = 6
                    for (i in 1 until cols) {
                        val x = w * i / cols
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    }
                    // horizontal grid
                    val rows = 4
                    for (i in 1 until rows) {
                        val y = h * i / rows
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    }
                    // gray session line
                    val sessionPath = Path().apply {
                        moveTo(0f, h * 0.5f)
                        cubicTo(w*0.2f, h*0.45f, w*0.35f, h*0.6f, w*0.5f, h*0.55f)
                        cubicTo(w*0.65f, h*0.5f, w*0.8f, h*0.45f, w, h*0.5f)
                    }
                    drawPath(
                        sessionPath,
                        color = Color(0xFFCBD5E1),
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                    // blue player line
                    val playerPath = Path().apply {
                        moveTo(0f, h * 0.7f)
                        cubicTo(w*0.15f, h*0.8f, w*0.3f, h*0.55f, w*0.45f, h*0.65f)
                        cubicTo(w*0.6f, h*0.75f, w*0.8f, h*0.35f, w, h*0.7f)
                    }
                    drawPath(
                        playerPath,
                        color = BluePrimary,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
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
