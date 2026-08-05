/*
 * File: BlockchainLedgerScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.admin

// ============================================================
// BlockchainLedgerScreen.kt
// Smart Garment Factory - Industry 4.0
// ============================================================
// Displays the cryptographic Blockchain Safety Ledger.
// Each block represents an immutable emergency report that has
// been hashed and linked cryptographically to the previous.
// Shows chain validity (tamper detection) in real-time.
// ============================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartfactory.ui.components.ShinyButton
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import kotlinx.coroutines.launch

@Composable
fun BlockchainLedgerScreen(navController: NavController) {

    var chain by remember { mutableStateOf<List<Block>>(emptyList()) }
    var isValid by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Load the chain from the FastAPI server when this screen opens
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.blockchainApi.getChain()
                chain = response.chain ?: emptyList()
                isValid = response.is_valid ?: false
            } catch (e: retrofit2.HttpException) {
                errorMessage = "Server Error ${e.code()}: Is main.py running on port 8000?"
            } catch (e: java.net.ConnectException) {
                errorMessage = "Network Error: Could not connect. Check server IP in RetrofitClient.kt"
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Dark gradient background
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Header ──────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔗 Blockchain Safety Ledger",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealMint
                )
                Text(
                    text = "Immutable cryptographic safety records",
                    fontSize = 13.sp,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(16.dp))

                ShinyButton(
                    text = "← Back to Dashboard",
                    onClick = { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Loading State ────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TealMint)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fetching blockchain from server...", color = MutedText)
                        }
                    }
                }
            }

            // ── Error State ──────────────────────────────────────────
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0010)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠ Connection Error",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF1744)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(errorMessage!!, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Run: uvicorn main:app --host 0.0.0.0 --port 8000 --reload",
                                color = TealMint,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // ── Validity Status Card ─────────────────────────────────
            if (!isLoading && errorMessage == null) {
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
                        Text(
                            text = if (isValid) "✅ Blockchain Integrity Verified — No Tampering Detected"
                            else "❌ Chain Integrity FAILED — Possible Tampering Detected!",
                            color = if (isValid) Color(0xFF00E676) else Color(0xFFFF1744),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                // ── Block List ───────────────────────────────────────
                items(chain) { block ->
                    BlockchainBlockCard(block = block)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ── Individual Block Card ──────────────────────────────────────────────────────
@Composable
fun BlockchainBlockCard(block: Block) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Block #${block.index ?: "?"}",
                    fontWeight = FontWeight.Bold,
                    color = TealMint,
                    fontSize = 16.sp
                )
                Text(
                    text = if (block.index == 1L) "GENESIS" else "RECORD",
                    color = if (block.index == 1L) MutedText else TealMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = BorderDark
            )

            // Report data (the actual safety record)
            Text(
                text = block.report_data ?: "Genesis Block",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cryptographic data
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
