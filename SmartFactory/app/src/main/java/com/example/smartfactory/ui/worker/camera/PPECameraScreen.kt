/*
 * File: PPECameraScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker.camera

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smartfactory.ai.AIPredictionManager
import com.example.smartfactory.model.PPEPrediction
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import kotlinx.coroutines.launch

@Composable
fun PPECameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val previewView = remember { PreviewView(context) }
    // CAMERA INTEGRATION: Image capture configuration
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
                                            "face_mask" to (!result.missing.contains("face-mask")),
                                            "hair_net" to (!result.missing.contains("hair-net")),
                                            "head_cover" to (!result.missing.contains("head-cover")),
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
                            Color(0xFF4A0010) // Dark red alert background
                        else 
                            SurfaceDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (ppeResult?.status == "WARNING") Color(0xFFFF1744) else BorderDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (errorMessage != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cancel, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text("AI PPE CHECK", style = MaterialTheme.typography.titleLarge, color = TealMint)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Face Mask Detection
                            val maskDetected = !ppeResult!!.missing.contains("face-mask")
                            RowItem(
                                label = "Face Mask:", 
                                isDetected = maskDetected
                            )
                            
                            // Note: Hair Net, Head Cover, and Person detection were removed 
                            // from the UI because the specific YOLO model you trained in Colab 
                            // only contains classes for Face Masks ('with_mask', 'without_mask').
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isSafe = ppeResult!!.status == "SAFE"
                                Icon(
                                    imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isSafe) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Status: ${if (isSafe) "SAFE" else "WARNING"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSafe) TealMint else MaterialTheme.colorScheme.error
                                )
                            }
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
fun RowItem(label: String, isDetected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isDetected) "Detected" else "Missing", 
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDetected) TealMint else Color(0xFFFF1744)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isDetected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isDetected) TealMint else Color(0xFFFF1744),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
