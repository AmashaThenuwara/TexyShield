package com.example.smartfactory.ui.camera

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
import com.example.smartfactory.model.PPEPrediction
import kotlinx.coroutines.launch

@Composable
fun PPECameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var ppeResult by remember { mutableStateOf<PPEPrediction?>(null) }
    var isPredicting by remember { mutableStateOf(false) }
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
            if (isPredicting) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Analyzing Image...", color = Color.White)
            } else {
                Button(
                    onClick = {
                        isPredicting = true
                        errorMessage = null
                        ppeResult = null
                        
                        ImageCaptureManager.takePhoto(
                            imageCapture = imageCapture,
                            context = context,
                            executor = ContextCompat.getMainExecutor(context),
                            onImageCaptured = { base64String ->
                                coroutineScope.launch {
                                    val result = AIPredictionManager.checkPPE(base64String)
                                    isPredicting = false
                                    if (result != null) {
                                        ppeResult = result
                                        
                                        // Save to Firebase under AIResults/PPE/worker01
                                        val database = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                                        val ppeData = mapOf(
                                            "helmet" to (result.detected.contains("helmet") || !result.missing.contains("helmet")),
                                            "vest" to (result.detected.contains("safety vest") || !result.missing.contains("safety vest")),
                                            "gloves" to (result.detected.contains("gloves") || !result.missing.contains("gloves")),
                                            "status" to result.status
                                        )
                                        database.child("AIResults").child("PPE").child("worker01").setValue(ppeData)
                                        
                                    } else {
                                        errorMessage = "Failed to get prediction from AI Server"
                                    }
                                }
                            },
                            onError = { exception ->
                                isPredicting = false
                                errorMessage = "Image capture failed: ${exception.message}"
                            }
                        )
                    }
                ) {
                    Text("Take Photo")
                }
            }
        }

        // Result UI Overlay
        if (ppeResult != null || errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ppeResult?.status == "WARNING") 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (errorMessage != null) {
                            Text("❌ Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("AI PPE CHECK", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Person Detection
                            val personDetected = ppeResult!!.detected.contains("person")
                            RowItem(
                                label = "Person:", 
                                status = if (personDetected) "Detected ✅" else "Missing ❌"
                            )
                            
                            // Helmet Detection
                            val helmetDetected = ppeResult!!.detected.contains("helmet") || !ppeResult!!.missing.contains("helmet")
                            RowItem(
                                label = "Helmet:", 
                                status = if (helmetDetected) "Detected ✅" else "Missing ❌"
                            )
                            
                            // Vest Detection
                            val vestDetected = ppeResult!!.detected.contains("safety vest") || !ppeResult!!.missing.contains("safety vest")
                            RowItem(
                                label = "Safety Vest:", 
                                status = if (vestDetected) "Detected ✅" else "Missing ❌"
                            )

                            // Gloves Detection
                            val glovesDetected = ppeResult!!.detected.contains("gloves") || !ppeResult!!.missing.contains("gloves")
                            RowItem(
                                label = "Gloves:", 
                                status = if (glovesDetected) "Detected ✅" else "Missing ❌"
                            )
                            
                            // Safety Shoes Detection
                            val shoesDetected = ppeResult!!.detected.contains("safety shoes") || !ppeResult!!.missing.contains("safety shoes")
                            RowItem(
                                label = "Safety Shoes:", 
                                status = if (shoesDetected) "Detected ✅" else "Missing ❌"
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Status:\n${if (ppeResult!!.status == "SAFE") "✅ SAFE" else "⚠ WARNING"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (ppeResult!!.status == "SAFE") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            ppeResult = null
                            errorMessage = null
                        }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowItem(label: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = status, style = MaterialTheme.typography.bodyLarge)
    }
}
