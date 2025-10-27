package com.example.figcompose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.figcompose.R
import com.example.figcompose.ui.theme.*
import com.example.figcompose.util.SessionManager
import com.example.figcompose.util.SessionDetailsState
import com.example.figcompose.util.ReportState
import com.example.figcompose.util.UsersManager
import com.example.figcompose.util.UsersState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(
    sessionId: Int,
    onBack: () -> Unit = {},
    onPlayerSelected: (Int, String, Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val state by sessionManager.detailState
    val reportState by sessionManager.reportState
    val usersManager = remember { UsersManager(context) }
    val usersState by usersManager.state
    val idToName = remember(usersState) {
        when (val us = usersState) {
            is UsersState.Loaded -> us.users.associate { it.id to (it.full_name ?: it.username ?: "User #${it.id}") }
            else -> emptyMap()
        }
    }

    LaunchedEffect(sessionId) {
        sessionManager.loadSession(sessionId)
        sessionManager.loadReport(sessionId)
        usersManager.loadUsers(limit = 200)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Session Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
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
            when (val s = state) {
                is SessionDetailsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = BluePrimary) }
                }
                is SessionDetailsState.Error -> {
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is SessionDetailsState.Loaded -> {
                    val session = s.session
                    Text(
                        text = session.title ?: "Session #${session.id}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                    val meta = listOfNotNull(session.session_type, session.location).joinToString(" • ")
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRow(label = "Start", value = session.start_time ?: "-")
                            InfoRow(label = "End", value = session.end_time ?: "-")
                            if (!session.description.isNullOrBlank()) {
                                InfoRow(label = "Notes", value = session.description ?: "-")
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    // Report sections
                    when (val r = reportState) {
                        is ReportState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = BluePrimary) }
                        }
                        is ReportState.Error -> {
                            Text(
                                text = r.message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        is ReportState.Loaded -> {
                            val report = r.report

                            // Build participants from union of survey, performances, and feedback
                            val surveyByPlayer = remember(report) { report.survey.associateBy { it.player_id ?: -1 } }
                            val participantIds = remember(report) {
                                buildSet {
                                    report.survey.mapNotNull { it.player_id }.forEach { add(it) }
                                    report.performances.mapNotNull { it.player_id }.forEach { add(it) }
                                    report.feedback.mapNotNull { it.player_id }.forEach { add(it) }
                                }
                            }

                            if (participantIds.isNotEmpty()) {
                                Text(
                                    text = "Players",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                participantIds.take(50).forEach { pid ->
                                    val sv = surveyByPlayer[pid]
                                    val resp = sv?.response ?: emptyMap()
                                    val rating = resp["rating"]?.toString() ?: "-"
                                    val condition = resp["condition"]?.toString()?.trim()
                                    val performance = resp["performance"]?.toString()?.trim()
                                    val ratingDisplay = if (rating == "-") "-" else "$rating/5"
                                    val displayName = idToName[pid] ?: "Player #$pid"

                                    val conditionColor = when (condition?.lowercase()) {
                                        "healthy" -> Color(0xFFD1FAE5)
                                        "injured" -> Color(0xFFFECACA)
                                        "tired" -> Color(0xFFFDE68A)
                                        else -> Color(0xFFE5E7EB)
                                    }
                                    val performanceColor = when (performance?.lowercase()) {
                                        "improved" -> Color(0xFFDBEAFE)
                                        "declined" -> Color(0xFFFECACA)
                                        "same" -> Color(0xFFE5E7EB)
                                        else -> Color(0xFFE5E7EB)
                                    }

                                    val tags = buildList {
                                        if (!condition.isNullOrBlank()) add(condition to conditionColor)
                                        if (!performance.isNullOrBlank()) add(performance to performanceColor)
                                    }

                                    PlayerRow(
                                        name = displayName,
                                        rating = ratingDisplay,
                                        tags = tags,
                                        onClick = { onPlayerSelected(pid, displayName, session.id) }
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PerfStat(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerRow(
    name: String,
    rating: String,
    tags: List<Pair<String, Color>>,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = "Go to details",
                    tint = TextSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("⭐ $rating") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFFFF3C4),
                        labelColor = TextPrimary
                    )
                )
                tags.forEach { (t, bg) ->
                    AssistChip(
                        onClick = {},
                        label = { Text(t) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = bg,
                            labelColor = TextPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SessionDetailsPreview() {
    FigcomposeTheme { SessionDetailsScreen(sessionId = 1) }
}
