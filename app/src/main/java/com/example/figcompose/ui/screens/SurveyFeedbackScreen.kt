package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.figcompose.ui.theme.BluePrimary
import com.example.figcompose.ui.theme.FigcomposeTheme
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary
import com.example.figcompose.util.SubmitSurveyState
import com.example.figcompose.util.SurveyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyFeedbackScreen(
    playerId: Int,
    playerName: String,
    sessionId: Int? = null,
    onBack: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val surveyManager = remember { SurveyManager(context) }
    val submitState by surveyManager.state

    var rating by remember { mutableStateOf(4) }
    var selectedCondition by remember { mutableStateOf(Condition.Healthy) }
    var performance by remember { mutableStateOf(PerformanceLevel.Improved) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Submit Feedback",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            SectionTitle("How was the session?")
            StarRating(rating = rating, onRatingChange = { rating = it })

            Spacer(Modifier.height(16.dp))
            SectionTitle("What's your current condition?")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Condition.values().forEach { c ->
                    val selected = c == selectedCondition
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clickable { selectedCondition = c },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        tonalElevation = 0.dp
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = c.label,
                                color = if (selected) BluePrimary else TextPrimary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("How did you perform?")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PerformanceCard(level = PerformanceLevel.Improved, selected = performance == PerformanceLevel.Improved) { performance = PerformanceLevel.Improved }
                PerformanceCard(level = PerformanceLevel.Steady, selected = performance == PerformanceLevel.Steady) { performance = PerformanceLevel.Steady }
                PerformanceCard(level = PerformanceLevel.NeedsWork, selected = performance == PerformanceLevel.NeedsWork) { performance = PerformanceLevel.NeedsWork }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("Additional Notes (Optional)")
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                placeholder = { Text("Anything else you want to share?") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))
            if (submitState is SubmitSurveyState.Error) {
                val msg = (submitState as SubmitSurveyState.Error).message
                Text(text = msg, color = Color(0xFFDC2626))
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (sessionId == null) {
                        Toast.makeText(context, "Sessions are not created", Toast.LENGTH_SHORT).show()
                    } else {
                        surveyManager.submit(
                            playerId = playerId,
                            sessionId = sessionId,
                            rating = rating,
                            condition = selectedCondition.label,
                            performance = performance.title,
                            notes = notes
                        ) { success, _ ->
                            if (success) onSubmitSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                if (submitState is SubmitSurveyState.Submitting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Feedback")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

enum class Condition(val label: String) { Healthy("Healthy"), Tired("Tired"), Injured("Injured") }

enum class PerformanceLevel(val title: String, val desc: String) {
    Improved("Improved", "Felt stronger and faster than before."),
    Steady("Steady", "Performance was consistent with my usual."),
    NeedsWork("Needs Work", "Felt a bit off and struggled with some drills.")
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        ),
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp)
    )
}

@Composable
private fun StarRating(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { i ->
            val filled = i <= rating
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (filled) Color(0xFFFFF7E5) else Color(0xFFF3F4F6))
                    .border(1.dp, if (filled) Color(0xFFFFE08A) else Color(0xFFE5E7EB), CircleShape)
                    .clickable { onRatingChange(i) },
                contentAlignment = Alignment.Center
            ) {
                Text("★", color = if (filled) Color(0xFFFFB800) else Color(0xFF9CA3AF))
            }
        }
    }
}

@Composable
private fun PerformanceCard(level: PerformanceLevel, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(2.dp, if (selected) BluePrimary else Color(0xFFCBD5E1), CircleShape)
                    .background(if (selected) BluePrimary else Color.Transparent, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(level.title, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(level.desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SurveyFeedbackScreenPreview() {
    FigcomposeTheme {
        SurveyFeedbackScreen(playerId = 1, playerName = "Ethan Carter")
    }
}
