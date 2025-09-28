package com.example.wear.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun StartSessionScreen(
    sessionName: String,
    playerName: String,
    onStart: () -> Unit
) {
    Card(
        onClick = {},
        enabled = false,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "New Session",
                style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            LabeledPill(label = "Session", value = sessionName)
            Spacer(Modifier.height(6.dp))
            LabeledPill(label = "Player", value = playerName)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text(text = "Start")
            }
        }
    }
}

@Composable
private fun LabeledPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.caption3)
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.caption2, fontWeight = FontWeight.SemiBold)
    }
}
