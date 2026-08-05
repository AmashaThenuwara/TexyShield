/*
 * File: ARAssistantScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker

// ============================================================
// ARAssistantScreen.kt — Smart Factory AR Machine Assistant
// ============================================================
// Mode A — Visual AI Detection:
//   Tap "Scan Machine" button → captures camera frame →
//   sends to AI backend (/machine-type-detection) →
//   displays machine type, confidence, safety warnings
// Mode B — QR Code (existing):
//   Scans MACHINE_xxx QR sticker → fetches Firebase sensor
//   data and machine health score → overlay as before
// ============================================================

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartfactory.api.RetrofitClient
import com.example.smartfactory.model.MachineTypeRequest
import com.example.smartfactory.model.MachineTypeResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Safety info data classes
data class MachineInfo(
    val displayName: String,
    val typicalModels: String,
    val safetyWarnings: List<String>,
    val usageSteps: List<String>,
    val hazards: List<String>
)

// ── Load machine safety JSON from assets ───────────────────────────────
fun loadMachineInfo(context: Context, machineType: String): MachineInfo? {
    return try {
        val json = context.assets.open("machine_safety_info.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        if (!root.has(machineType)) return null
        val obj = root.getJSONObject(machineType)

        val warnings = mutableListOf<String>()
        val steps    = mutableListOf<String>()
        val hazards  = mutableListOf<String>()

        val wArr = obj.getJSONArray("safety_warnings")
        for (i in 0 until wArr.length()) warnings.add(wArr.getString(i))

        val sArr = obj.getJSONArray("usage_steps")
        for (i in 0 until sArr.length()) steps.add(sArr.getString(i))

        val hArr = obj.getJSONArray("hazards")
        for (i in 0 until hArr.length()) hazards.add(hArr.getString(i))

        MachineInfo(
            displayName   = obj.optString("display_name", machineType),
            typicalModels = obj.optString("typical_models", "Unknown"),
            safetyWarnings = warnings,
            usageSteps     = steps,
            hazards        = hazards
        )
    } catch (e: Exception) {
        null
    }
}

// Bitmap → Base64 helper
fun bitmapToBase64(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
}

// Main composable
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun ARAssistantScreen() {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Camera
    val previewView   = remember { PreviewView(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // ImageCapture use case — for grabbing a frame on demand
    // CAMERA INTEGRATION: Image capture configuration
		val imageCapture = remember { ImageCapture.Builder().build() }

    // ── State ──────────────────────────────────────────────────────────
    var activeMode           by remember { mutableStateOf("IDLE") } // IDLE | SCANNING | RESULT | QR_RESULT
    var isAnalysing          by remember { mutableStateOf(false) }
    var detectionResult      by remember { mutableStateOf<MachineTypeResult?>(null) }
    var machineInfo          by remember { mutableStateOf<MachineInfo?>(null) }
    var errorMessage         by remember { mutableStateOf<String?>(null) }

    // QR / Firebase state (kept from original)
    var scannedMachineId     by remember { mutableStateOf<String?>(null) }
    var machineTemperature   by remember { mutableStateOf("--") }
    var machineGas           by remember { mutableStateOf("--") }
    var machineStatus        by remember { mutableStateOf("...") }
    var healthScore          by remember { mutableStateOf("...") }
    var aiRecommendation     by remember { mutableStateOf("") }

    // Animated scan ring
    val scanRingAlpha by animateFloatAsState(
        targetValue = if (isAnalysing) 0.2f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scanRing"
    )

    // Firebase listener for QR mode
    LaunchedEffect(scannedMachineId) {
        if (scannedMachineId != null) {
            activeMode = "QR_RESULT"
            val db = FirebaseDatabase.getInstance().reference
            db.child("machines").child(scannedMachineId!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    machineTemperature = snapshot.child("temperature").getValue(String::class.java) ?: "--"
                    machineGas         = snapshot.child("gas").getValue(String::class.java) ?: "--"
                    machineStatus      = snapshot.child("status").getValue(String::class.java) ?: "UNKNOWN"
                    coroutineScope.launch {
                        try {
                            val result = RetrofitClient.api.getMachineHealth(
                                com.example.smartfactory.model.MachineHealthRequest(
                                    air_temperature     = 298.1,
                                    process_temperature = 308.6,
                                    rotational_speed    = 1551.0,
                                    torque              = 42.8,
                                    tool_wear           = 10.0
                                )
                            )
                            machineStatus      = result.status
                            aiRecommendation   = result.recommendation
                            healthScore        = "${result.health_score}%"
                        } catch (_: Exception) { machineStatus = "AI OFFLINE" }
                    }
                }
                override fun onCancelled(error: DatabaseError) { machineStatus = "ERROR" }
            })
        }
    }

    // Camera setup
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // QR analyzer still active in background
            val qrAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context), QRCodeAnalyzer { qrResult ->
                        if (qrResult.startsWith("MACHINE_") && activeMode == "IDLE") {
                            scannedMachineId = qrResult
                        }
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    qrAnalyzer
                )
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(context))
    }

    // AI Scan action
    fun scanWithAI() {
        if (isAnalysing) return
        isAnalysing   = true
        activeMode    = "SCANNING"
        errorMessage  = null
        detectionResult = null
        machineInfo   = null

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val bitmap  = imageProxy.toBitmap()
                            imageProxy.close()
                            val b64     = bitmapToBase64(bitmap)
                            val result  = RetrofitClient.api.detectMachineType(
                                MachineTypeRequest(image_path = b64)
                            )
                            val info = loadMachineInfo(context, result.machine_type)
                            withContext(Dispatchers.Main) {
                                detectionResult = result
                                machineInfo     = info
                                activeMode      = "RESULT"
                                isAnalysing     = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "AI server error: ${e.message}"
                                activeMode   = "IDLE"
                                isAnalysing  = false
                            }
                        }
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    isAnalysing = false
                    activeMode  = "IDLE"
                    errorMessage = "Camera error: ${exception.message}"
                }
            }
        )
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {

        // Camera background
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Dark top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC000000), Color.Transparent)
                    )
                )
        )

        // Dark bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xDD000000))
                    )
                )
        )

        // Top header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, contentDescription = null,
                    tint = TealMint, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "AR Machine Assistant",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                when (activeMode) {
                    "SCANNING"   -> "Analysing machine..."
                    "RESULT"     -> "Machine identified ✓"
                    "QR_RESULT"  -> "QR code detected ✓"
                    else         -> "Point at machine • Tap Scan or show QR"
                },
                color = TealMint,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Scanning reticle (shown when IDLE or SCANNING)
        if (activeMode == "IDLE" || activeMode == "SCANNING") {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center)
                    .drawBehind {
                        val strokeW = 3.dp.toPx()
                        val cornerR = 24.dp.toPx()
                        val len     = 40.dp.toPx()
                        val color   = androidx.compose.ui.graphics.Color(
                            red = 0f, green = 0.898f, blue = 1f, alpha = scanRingAlpha
                        )
                        // Draw corner brackets
                        listOf(
                            Offset(0f, 0f), Offset(size.width - len, 0f),
                            Offset(0f, size.height - len), Offset(size.width - len, size.height - len)
                        ).forEachIndexed { idx, offset ->
                            drawRoundRect(
                                color = color,
                                topLeft = offset,
                                size = Size(len, strokeW),
                                cornerRadius = CornerRadius(cornerR),
                                style = Stroke(width = strokeW)
                            )
                            drawRoundRect(
                                color = color,
                                topLeft = offset,
                                size = Size(strokeW, len),
                                cornerRadius = CornerRadius(cornerR),
                                style = Stroke(width = strokeW)
                            )
                        }
                    }
            )
        }

        // Scanning spinner
        if (activeMode == "SCANNING") {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(130.dp))
                CircularProgressIndicator(
                    color = TealMint,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Sending to AI...", color = TealMint, fontSize = 13.sp)
            }
        }

        // ── Error toast ───────────────────────────────────────────────
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC2D0000)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "⚠ $errorMessage",
                    color = Color(0xFFFF5252),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        }

        // AI Detection Result Panel
        AnimatedVisibility(
            visible = activeMode == "RESULT",
            enter = fadeIn() + expandIn(),
            exit  = fadeOut() + shrinkOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            if (detectionResult != null) {
                val result = detectionResult!!
                val info   = machineInfo
                val conf   = result.confidence
                val confColor = when {
                    conf >= 75 -> Color(0xFF00E676)
                    conf >= 50 -> Color(0xFFFFD740)
                    else       -> Color(0xFFFF5252)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Futuristic Leader Lines & Reticle Canvas
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // Draw target reticle crosshairs
                        drawCircle(
                            color = Color(0x3300E676),
                            radius = 60.dp.toPx(),
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF00E676),
                            radius = 3.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        
                        // Thin target crosshair marks
                        drawLine(Color(0xFF00E676), Offset(centerX - 70.dp.toPx(), centerY), Offset(centerX - 50.dp.toPx(), centerY), 1.dp.toPx())
                        drawLine(Color(0xFF00E676), Offset(centerX + 50.dp.toPx(), centerY), Offset(centerX + 70.dp.toPx(), centerY), 1.dp.toPx())
                        drawLine(Color(0xFF00E676), Offset(centerX, centerY - 70.dp.toPx()), Offset(centerX, centerY - 50.dp.toPx()), 1.dp.toPx())
                        drawLine(Color(0xFF00E676), Offset(centerX, centerY + 50.dp.toPx()), Offset(centerX, centerY + 70.dp.toPx()), 1.dp.toPx())

                        // Leader Line 1: Top-Left Card (Needle Hazard) to Reticle Left Area
                        drawLine(
                            color = Color(0xAAFF5252),
                            start = Offset(130.dp.toPx(), 260.dp.toPx()),
                            end = Offset(centerX - 80.dp.toPx(), centerY - 40.dp.toPx()),
                            strokeWidth = 1.2.dp.toPx()
                        )
                        drawCircle(Color(0xFFFF5252), 4.dp.toPx(), Offset(centerX - 80.dp.toPx(), centerY - 40.dp.toPx()))

                        // Leader Line 2: Center-Right Card (Bobbin Hazard) to Reticle Lower Right Area
                        drawLine(
                            color = Color(0xAAFFD740),
                            start = Offset(size.width - 130.dp.toPx(), 390.dp.toPx()),
                            end = Offset(centerX + 80.dp.toPx(), centerY + 30.dp.toPx()),
                            strokeWidth = 1.2.dp.toPx()
                        )
                        drawCircle(Color(0xFFFFD740), 4.dp.toPx(), Offset(centerX + 80.dp.toPx(), centerY + 30.dp.toPx()))

                        // Leader Line 3: Bottom-Left Card (Usage Step) to Reticle Bottom-Left Area
                        drawLine(
                            color = Color(0xAA00E676),
                            start = Offset(130.dp.toPx(), size.height - 230.dp.toPx()),
                            end = Offset(centerX - 60.dp.toPx(), centerY + 90.dp.toPx()),
                            strokeWidth = 1.2.dp.toPx()
                        )
                        drawCircle(Color(0xFF00E676), 4.dp.toPx(), Offset(centerX - 60.dp.toPx(), centerY + 90.dp.toPx()))
                    }

                    // 2. Primary Header Overlay (Holographic Dashboard)
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF00C853), CircleShape)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "AI ANALYSIS ACTIVE",
                                        color = Color(0xFF00C853),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = info?.displayName ?: result.machine_type,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = info?.typicalModels ?: "Dynamic Industrial Node",
                                    color = TealMint,
                                    fontSize = 10.sp
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = confColor.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, confColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("${conf.toInt()}%", color = confColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("CONF.", color = confColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3. Floating Card 1: Needle Area Hazard (Top-Left Position)
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 180.dp, start = 16.dp)
                            .width(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ HAZARD: NEEDLE",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = info?.hazards?.firstOrNull() ?: "Moving active needle zone",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = info?.safetyWarnings?.firstOrNull() ?: "Keep fingers 5cm clear.",
                                color = Color.LightGray,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // 4. Floating Card 2: Bobbin Pinch Area (Center-Right Position)
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 280.dp, end = 16.dp)
                            .width(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ HAZARD: PINCH",
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = info?.hazards?.getOrNull(1) ?: "Bobbin area pinch zone",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = info?.safetyWarnings?.getOrNull(1) ?: "Ensure guard is fitted.",
                                color = Color.LightGray,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // 5. Floating Card 3: Usage Guidelines (Bottom-Left Position)
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 160.dp, start = 16.dp)
                            .width(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚙️ CORRECT USAGE",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = info?.usageSteps?.firstOrNull() ?: "Initialize servomotor.",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = info?.usageSteps?.getOrNull(1) ?: "Load bobbin properly.",
                                color = Color.LightGray,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // 6. HUD Bottom Control Panel (Database, Settings, Visual Manuals, Close)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .fillMaxWidth(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // High tech bottom buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
                                border = BorderStroke(0.5.dp, BorderDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("Database", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
                                border = BorderStroke(0.5.dp, BorderDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("Settings", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
                                border = BorderStroke(0.5.dp, BorderDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("Visual Manuals", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))

                        // High Tech EXIT AR BUTTON
                        Button(
                            onClick = { activeMode = "IDLE"; detectionResult = null; machineInfo = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCFF5252)),
                            border = BorderStroke(1.dp, Color(0xFFFF5252)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("EXIT AR VIEW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // QR Result Panel (existing behaviour, preserved)
        AnimatedVisibility(
            visible = activeMode == "QR_RESULT" && scannedMachineId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (machineStatus == "CRITICAL")
                        Color(0xCC2D0000) else SurfaceDark.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (machineStatus == "CRITICAL") Color(0xFFFF5252) else BorderDark)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = TealMint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("${scannedMachineId}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoRow("Temperature", "$machineTemperature °C", Color.White)
                    InfoRow("Gas Level", "$machineGas ppm", Color.White)
                    InfoRow("Status", machineStatus, if (machineStatus == "GOOD" || machineStatus == "SAFE") Color(0xFF69F0AE) else Color(0xFFFF5252))
                    if (healthScore != "...") {
                        InfoRow("AI Health Score", healthScore, TealMint)
                        Spacer(Modifier.height(8.dp))
                        Text("💡 $aiRecommendation", color = Color(0xFFFFD740), fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { scannedMachineId = null; activeMode = "IDLE" },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, TealMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = TealMint)
                    }
                }
            }
        }

        // Bottom scan button (visible when IDLE)
        if (activeMode == "IDLE") {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { scanWithAI() },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = TealMint),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan Machine",
                        tint = DeepDarkBg, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Scan Machine", color = TealMint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Reusable info row
@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// QR Code Analyzer (unchanged)
class QRCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { onQrCodeScanned(it) }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}
