/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.R
import com.example.wear.presentation.theme.NativesensorTheme
import com.example.wear.sensor.SensorDataManager

class MainActivity : ComponentActivity() {
    private lateinit var sensorDataManager: SensorDataManager
    private var isCollecting = mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startSensorCollection()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        sensorDataManager = SensorDataManager(this)
        
        // Set a default user ID (in real app, this would come from login)
        sensorDataManager.setUserId(1)
        
        setTheme(android.R.style.Theme_DeviceDefault)
        
        setContent {
            WearApp(
                isCollecting = isCollecting.value,
                onStartCollection = { requestPermissions() },
                onStopCollection = { stopSensorCollection() }
            )
        }
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        
        val permissionsToRequest = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            startSensorCollection()
        }
    }
    
    private fun startSensorCollection() {
        sensorDataManager.startDataCollection()
        isCollecting.value = true
    }
    
    private fun stopSensorCollection() {
        sensorDataManager.stopDataCollection()
        isCollecting.value = false
    }
}

@Composable
fun WearApp(
    isCollecting: Boolean,
    onStartCollection: () -> Unit,
    onStopCollection: () -> Unit
) {
    NativesensorTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeText()
                
                Text(
                    text = if (isCollecting) "Collecting Data..." else "Sensor Monitor",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = if (isCollecting) onStopCollection else onStartCollection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isCollecting) "Stop Collection" else "Start Collection",
                        style = MaterialTheme.typography.button
                    )
                }
                
                if (isCollecting) {
                    Text(
                        text = "Sending data to server...",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(
        isCollecting = false,
        onStartCollection = {},
        onStopCollection = {}
    )
}