package com.example.figcompose.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestMetricsPermissions(
    onPermissionsGranted: () -> Unit = {},
    onPermissionsChanged: (activityGranted: Boolean, locationGranted: Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var activityGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val activity = results[Manifest.permission.ACTIVITY_RECOGNITION] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        val fine = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        activityGranted = activity
        // Require FINE for GPS provider usage; COARSE alone is not sufficient
        locationGranted = fine

        onPermissionsChanged(activityGranted, locationGranted)
        if (activityGranted && locationGranted) onPermissionsGranted()
    }

    LaunchedEffect(Unit) {
        val activity = ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        activityGranted = activity
        locationGranted = fine
        onPermissionsChanged(activityGranted, locationGranted)

        val allGranted = activity && fine
        if (allGranted) {
            onPermissionsGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}
