/*
 * File: QRAttendanceScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartfactory.api.AttendanceData
import com.example.smartfactory.api.Block
import com.example.smartfactory.api.RetrofitClient
import com.example.smartfactory.firebase.AttendanceManager
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class AttendanceRecord(
    val timestamp: Long = 0,
    val name: String = "",
    val uid: String = ""
)

@Composable
fun QRAttendanceScreen() {
    val scope = rememberCoroutineScope()
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var blockchainLogs by remember { mutableStateOf<List<Block>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    fun loadBlockchain() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.blockchainApi.getAttendanceChain()
                blockchainLogs = response.chain?.filter { it.index != 1L }?.reversed() ?: emptyList()
            } catch (e: Exception) {
                // Silently handle - show empty state
            } finally {
                isLoading = false
            }
        }
    }

    var lastScannedName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadBlockchain()
        
        AttendanceManager.listenAndProcessESP32Scans(
            onProcessed = { message ->
                lastScannedName = message
            },
            onMiningCompleted = {
                loadBlockchain()
            },
            onError = { /* ignore errors */ }
        )
    }

    LaunchedEffect(lastScannedName) {
        if (lastScannedName != null) {
            kotlinx.coroutines.delay(5000)
            lastScannedName = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AttendanceManager.stopScanListener()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(DeepDarkBg, Color(0xFF0A1520))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Text(
            text = "Attendance Log",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Immutable blockchain record • Cannot be edited",
            color = TealMint,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info + Log Button Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TealMint, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Blockchain Protected Attendance",
                        color = TealMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the button below to log your attendance manually. Alternatively, present your Profile QR Code in front of the entrance camera. You will hear a beep tone once scanned successfully.",
                    color = MutedText,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isSaving) return@Button
                        saveError = null
                        isSaving = true
                        scope.launch {
                            try {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                val now = java.util.Date()
                                val data = AttendanceData(
                                    worker_uid = currentUserUid.ifEmpty { "UNKNOWN" },
                                    worker_name = currentUserUid.take(8),
                                    timestamp = sdf.format(now),
                                    shift = "Morning"
                                )
                                RetrofitClient.blockchainApi.mineAttendanceBlock(data)
                                loadBlockchain()
                            } catch (e: Exception) {
                                saveError = "Server unreachable. Make sure backend is running."
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealMint),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = DeepDarkBg, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (isSaving) "Logging to Blockchain..." else "✦ Log Attendance Now",
                        color = DeepDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (saveError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(saveError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📷 Live Camera Feed from PC Backend Proxy
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📷 Live Entrance Camera Feed",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E676), RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Backend Proxied",
                            color = MutedText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { context ->
                        android.webkit.WebView(context).apply {
                            clearCache(true)
                            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.javaScriptEnabled = true
                            setBackgroundColor(0)
                            isVerticalScrollBarEnabled = false
                            isHorizontalScrollBarEnabled = false
                            loadUrl("${RetrofitClient.BASE_URL}stream")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        if (lastScannedName != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TealMint.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TealMint)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TealMint
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "QR Code Scanned Successfully!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                lastScannedName!!,
                                color = TealMint,
                                fontSize = 12.sp
                            )
                        }
                    }
                    TextButton(onClick = { lastScannedName = null }) {
                        Text("Dismiss", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Logs",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { loadBlockchain() }) {
                Text("Refresh", color = TealMint, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logs List
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TealMint, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading blockchain...", color = MutedText, fontSize = 13.sp)
                }
            }
        } else if (blockchainLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MutedText, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No attendance logs yet.", color = Color.Gray, fontSize = 14.sp)
                    Text("Tap 'Log Attendance Now' to start.", color = MutedText, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(blockchainLogs) { block ->
                    AttendanceCard(block)
                }
            }
        }
    }
}

@Composable
fun AttendanceCard(block: Block) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(TealMint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealMint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Attendance Logged", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(block.report_data ?: "Unknown Date/Time", color = MutedText, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "Block #${block.index} · Tx: ${block.previous_hash?.take(14)}...",
                    color = Color(0xFF14B8A6),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Icon(Icons.Default.Lock, contentDescription = null, tint = TealMint.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
        }
    }
}