package com.example.smartfactory.ui.camera

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermission(
    content: @Composable () -> Unit
){
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit){
        permissionState.launchPermissionRequest()
    }

    if (permissionState.status.isGranted) {
        content()
    }
}
