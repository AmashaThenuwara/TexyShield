/*
 * File: AttendanceLedgerScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.navigation.NavController
import com.example.smartfactory.api.Block
import com.example.smartfactory.api.RetrofitClient
import com.example.smartfactory.firebase.AttendanceManager
import com.example.smartfactory.ui.components.ShinyButton
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import kotlinx.coroutines.launch

@Composable
fun AttendanceLedgerScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var blockchain by remember { mutableStateOf<List<Block>>(emptyList()) }
    var isValid by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchAttendanceBlockchain() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = RetrofitClient.blockchainApi.getAttendanceChain()
                blockchain = response.chain ?: emptyList()
                isValid = response.is_valid ?: true
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to fetch attendance blockchain. Is the server running? ${e.message}"
                isLoading = false
            }
        }
    }

    var lastScannedName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fetchAttendanceBlockchain()
        
        AttendanceManager.listenAndProcessESP32Scans(
            onProcessed = { message ->
                lastScannedName = message
            },
            onMiningCompleted = {
                fetchAttendanceBlockchain()
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
        colors = listOf(DeepDarkBg, Color(0xFF0C1014))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "🔗 Attendance Blockchain Ledger",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealMint
                    )
                    Text(
                        text = "Immutable cryptography for worker attendance",
                        fontSize = 13.sp,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ShinyButton(
                        text = "↻ Refresh Blockchain",
                        onClick = { fetchAttendanceBlockchain() }
                    )
                }
            }

            // 📷 Live Camera Feed from ESP32-CAM (IP: 192.168.43.144)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(bottom = 20.dp),
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
                                fontSize = 14.sp
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
                                    fontSize = 11.sp,
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
            }

            if (lastScannedName != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TealMint)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fetching attendance blockchain from server...", color = MutedText)
                        }
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0010)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Connection Error", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage!!, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isValid) SurfaceDark else Color(0xFF4A0010)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isValid) Color(0xFF00E676) else Color(0xFFFF1744)
                        )
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (isValid) Color(0xFF00E676) else Color(0xFFFF1744)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Chain Integrity Status",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isValid) "VALID - NO TAMPERING DETECTED" else "INVALID - CHAIN TAMPERED!",
                                    color = if (isValid) Color(0xFF00E676) else Color(0xFFFF1744),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                items(blockchain.reversed()) { block ->
                    AttendanceBlockCard(block)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AttendanceBlockCard(block: Block) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Block #${block.index ?: "?"}",
                    fontWeight = FontWeight.Bold,
                    color = TealMint,
                    fontSize = 16.sp
                )
                Text(
                    text = if (block.index == 1L) "GENESIS" else "ATTENDANCE RECORD",
                    color = if (block.index == 1L) MutedText else TealMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = BorderDark
            )

            Text(
                text = block.report_data ?: "No Data",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Proof of Work: ${block.proof}",
                color = MutedText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Prev Hash: ${block.previous_hash?.take(32) ?: "N/A"}...",
                color = MutedText,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
