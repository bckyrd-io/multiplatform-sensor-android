package com.example.figcompose.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.figcompose.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    onBack: () -> Unit = {},
    onCreateSession: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    
    // Form state
    var title by remember { mutableStateOf("") }
    var sessionType by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<Calendar?>(null) }
    var endTime by remember { mutableStateOf<Calendar?>(null) }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    val sessionTypes = listOf("Technical", "Tactical", "Physical", "Match", "Recovery", "Other")
    
    // Time formatter
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    // Time picker function
    fun showTimePicker(isStartTime: Boolean, onTimeSelected: (Calendar) -> Unit) {
        val currentTime = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val newTime = Calendar.getInstance()
                newTime.set(Calendar.HOUR_OF_DAY, hour)
                newTime.set(Calendar.MINUTE, minute)
                onTimeSelected(newTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            false // 24-hour format
        ).show()
    }
    
    // Validation
    val isFormValid = title.isNotBlank() && 
                     sessionType.isNotBlank() && 
                     startTime != null && 
                     endTime != null &&
                     location.isNotBlank()
    
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
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
                .verticalScroll(rememberScrollState())
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Session Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Session Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sessionType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Session Type") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sessionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                sessionType = type
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Start Time
                OutlinedButton(
                    onClick = {
                        showTimePicker(true) { time ->
                            startTime = time
                            // If end time is not set, set it to 1 hour after start
                            if (endTime == null) {
                                val end = time.clone() as Calendar
                                end.add(Calendar.HOUR, 1)
                                endTime = end
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Start Time",
                        tint = if (startTime != null) BluePrimary else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = startTime?.let { timeFormat.format(it.time) } ?: "Start Time",
                        color = if (startTime != null) TextPrimary else TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // End Time
                OutlinedButton(
                    onClick = {
                        showTimePicker(false) { time ->
                            // Ensure end time is after start time
                            startTime?.let { start ->
                                if (time.after(start)) {
                                    endTime = time
                                } else {
                                    // If end time is before start, show error or adjust
                                    val adjustedTime = start.clone() as Calendar
                                    adjustedTime.add(Calendar.HOUR, 1)
                                    endTime = adjustedTime
                                }
                            } ?: run {
                                // If no start time set, just set the end time
                                endTime = time
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "End Time",
                        tint = if (endTime != null) BluePrimary else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = endTime?.let { timeFormat.format(it.time) } ?: "End Time",
                        color = if (endTime != null) TextPrimary else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location Input
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes Input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Submit Button
            Button(
                onClick = {
                    val startTimeStr = startTime?.let { time ->
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(time.time)
                    } ?: ""
                    
                    val endTimeStr = endTime?.let { time ->
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(time.time)
                    } ?: ""
                    
                    onCreateSession(
                        title,
                        sessionType,
                        startTimeStr,
                        endTimeStr,
                        location,
                        notes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) BluePrimary else Color.LightGray
                ),
                enabled = isFormValid
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