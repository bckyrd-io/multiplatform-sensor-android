package com.example.wear.presentation.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestSensorPermissions(
    onPermissionsGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }
    
    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,           // Heart rate sensor
        Manifest.permission.ACTIVITY_RECOGNITION,   // Step counter sensor
        Manifest.permission.ACCESS_FINE_LOCATION    // GPS for accurate distance/speed
    )
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        permissionsGranted = permissionsMap.values.all { it }
        if (permissionsGranted) {
            onPermissionsGranted()
        }
    }
    
    LaunchedEffect(Unit) {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            permissionsGranted = true
            onPermissionsGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}
