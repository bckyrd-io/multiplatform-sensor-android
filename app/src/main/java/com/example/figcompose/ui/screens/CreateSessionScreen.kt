package com.example.figcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.figcompose.ui.theme.BluePrimary
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    onBack: () -> Unit = {},
    onCreateSession: (String, String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    var sessionName by remember { mutableStateOf("") }
    var sessionType by remember { mutableStateOf("Training") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val sessionTypes = listOf("Training", "Match", "Team Meeting", "Other")
    var showSessionTypeDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Create New Session",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Session Name
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Session Name",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter session name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = BluePrimary,
                        cursorColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Session Type Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Session Type",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { showSessionTypeDropdown = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            sessionType,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select session type",
                            tint = TextSecondary
                        )
                    }
                }
                
                if (showSessionTypeDropdown) {
                    DropdownMenu(
                        expanded = showSessionTypeDropdown,
                        onDismissRequest = { showSessionTypeDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sessionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    sessionType = type
                                    showSessionTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Date Picker
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Date",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date",
                            tint = BluePrimary
                        )
                        Text(
                            if (date.isNotEmpty()) date else "Select date",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (date.isNotEmpty()) TextPrimary else TextSecondary
                            )
                        )
                    }
                }
                
                // Date picker dialog would go here
                // For now, we'll just use a text input
                if (showDatePicker) {
                    // In a real app, you would show a date picker dialog here
                    // For now, we'll just use a text input
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        placeholder = { Text("MM/DD/YYYY") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Start Time
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Start Time",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { showStartTimePicker = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
imageVector = Icons.Default.DateRange,
                            contentDescription = "Select start time",
                            tint = BluePrimary
                        )
                        Text(
                            if (startTime.isNotEmpty()) startTime else "Select start time",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (startTime.isNotEmpty()) TextPrimary else TextSecondary
                            )
                        )
                    }
                }
                
                if (showStartTimePicker) {
                    // Time picker dialog would go here
                    // For now, we'll just use a text input
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        placeholder = { Text("HH:MM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // End Time
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "End Time",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { showEndTimePicker = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
imageVector = Icons.Default.DateRange,
                            contentDescription = "Select end time",
                            tint = BluePrimary
                        )
                        Text(
                            if (endTime.isNotEmpty()) endTime else "Select end time",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (endTime.isNotEmpty()) TextPrimary else TextSecondary
                            )
                        )
                    }
                }
                
                if (showEndTimePicker) {
                    // Time picker dialog would go here
                    // For now, we'll just use a text input
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        placeholder = { Text("HH:MM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Location
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Location",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = BluePrimary,
                        cursorColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Notes
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Notes",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = { Text("Add any additional notes...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = BluePrimary,
                        cursorColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Session Button
            Button(
                onClick = {
                    if (sessionName.isNotBlank() && date.isNotBlank() && 
                        startTime.isNotBlank() && endTime.isNotBlank()) {
                        onCreateSession(sessionName, sessionType, date, startTime, endTime, location, notes)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = sessionName.isNotBlank() && date.isNotBlank() && 
                         startTime.isNotBlank() && endTime.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(
                    "Create Session",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateSessionScreenPreview() {
    CreateSessionScreen()
}
