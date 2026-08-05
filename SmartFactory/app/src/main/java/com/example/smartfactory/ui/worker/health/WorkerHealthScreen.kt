/*
 * File: WorkerHealthScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker.health

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartfactory.ai.AIPredictionManager
import com.example.smartfactory.ui.worker.camera.ImageCaptureManager
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import kotlinx.coroutines.launch

@Composable
fun WorkerHealthScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val previewView = remember { PreviewView(context) }
    // CAMERA INTEGRATION: Image capture configuration
		val imageCapture = remember { ImageCapture.Builder().build() }
    
    var posture by remember { mutableStateOf("Waiting...") }
    var fatigue by remember { mutableStateOf("Waiting...") }
    var facialState by remember { mutableStateOf("Waiting...") }
    var stress by remember { mutableStateOf("Waiting...") }
    var status by remember { mutableStateOf("Waiting...") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            // CAMERA INTEGRATION: Setting up camera selector for special UI features
		val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                errorMessage = "Camera bind failed: ${exc.message}"
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Capture Button & Loading Indicator
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Analyzing Image...", color = Color.White)
            } else {
                Button(
                    onClick = {
                        loading = true
                        errorMessage = null
                        
                        ImageCaptureManager.takePhoto(
                            imageCapture = imageCapture,
                            context = context,
                            executor = ContextCompat.getMainExecutor(context),
                            onImageCaptured = { base64String ->
                                scope.launch {
                                    val result = AIPredictionManager.checkWorkerHealth(base64String)
                                    loading = false
                                    if (result != null) {
                                        posture = result.posture
                                        fatigue = result.fatigue
                                        stress = result.stress
                                        status = result.status
                                        facialState = result.facial_state
                                    } else {
                                        errorMessage = "Failed to get prediction from AI Server"
                                    }
                                }
                            },
                            onError = { exception ->
                                loading = false
                                errorMessage = "Image capture failed: ${exception.message}"
                            }
                        )
                    }
                ) {
                    Text("Take Photo")
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (posture != "Waiting...") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "AI Worker Health",
                            style = MaterialTheme.typography.titleLarge,
                            color = TealMint
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Posture : $posture", color = Color.White)
                        Text("Fatigue : $fatigue", color = Color.White)
                        Text("Facial Expression : $facialState", color = Color.White)
                        Text("Stress Level : $stress", color = Color.White)
                        Text("Status : $status", color = if(status == "REST_RECOMMENDED") Color(0xFFFF1744) else TealMint)
                    }
                }
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0010)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744))) {
                    Text(
                        text = errorMessage ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFF1744)
                    )
                }
            }
        }
    }
}
