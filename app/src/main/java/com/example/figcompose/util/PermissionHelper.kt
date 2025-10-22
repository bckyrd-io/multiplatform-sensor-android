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
fun RequestMetricsPermissions(onPermissionsGranted: () -> Unit = {}) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        if (granted) onPermissionsGranted()
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACTIVITY_RECOGNITION
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            granted = true
            onPermissionsGranted()
        } else {
            launcher.launch(permission)
        }
    }
}
