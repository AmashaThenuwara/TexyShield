package com.example.smartfactory.ui.health

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
import com.example.smartfactory.ui.camera.ImageCaptureManager
import kotlinx.coroutines.launch

@Composable
fun WorkerHealthScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var posture by remember { mutableStateOf("Waiting...") }
    var fatigue by remember { mutableStateOf("Waiting...") }
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
                                        status = result.status
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
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "AI Worker Health",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Posture : $posture")
                        Text("Fatigue : $fatigue")
                        Text("Status : $status")
                    }
                }
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = errorMessage ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
