/*
 * File: WorkerDashboard.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.smartfactory.firebase.AlertManager
import com.example.smartfactory.model.Alert
import kotlinx.coroutines.delay

@Composable
fun WorkerDashboard(navController: NavController) {
    var currentAlert by remember { mutableStateOf(Alert()) }
    var isVisible by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        AlertManager.listenToCurrentAlert(
            onAlertChanged = { alert -> currentAlert = alert },
            onError = { }
        )
        onDispose { AlertManager.stopListening() }
    }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            com.example.smartfactory.ui.theme.DeepDarkBg,
            Color(0xFF0B0E14)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Mockup Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        com.example.smartfactory.ui.theme.TealMint.copy(alpha = 0.2f),
                                        Color(0xFF1E293B)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "Worker",
                            tint = com.example.smartfactory.ui.theme.TealMint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 12.sp,
                            color = com.example.smartfactory.ui.theme.MutedText
                        )
                        Text(
                            text = "Factory Worker 👋",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Notification Bell with Green Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(com.example.smartfactory.ui.theme.DeepDarkSurface)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(com.example.smartfactory.ui.theme.TealMint)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Emergency Alert Banner
            AnimatedVisibility(
                visible = currentAlert.type != "NONE" && currentAlert.type.isNotBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0010)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, com.example.smartfactory.ui.theme.ErrorRed)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = com.example.smartfactory.ui.theme.ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "EMERGENCY EVACUATION ALERT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.smartfactory.ui.theme.ErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentAlert.message, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // Protection Level Card (Total Balance Style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E1E)),
                border = BorderStroke(1.dp, com.example.smartfactory.ui.theme.TealMint.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0A1D1B), Color(0xFF08090C))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Safety & Compliance Level",
                            fontSize = 13.sp,
                            color = com.example.smartfactory.ui.theme.MutedText
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "98.7%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "▲ 1.45%",
                                color = com.example.smartfactory.ui.theme.TealMint,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "STATUS: FULLY PROTECTED",
                            fontSize = 11.sp,
                            color = com.example.smartfactory.ui.theme.TealMint,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Grid (Top Up, Send Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionPill(
                    title = "Attendance",
                    icon = Icons.Default.CheckCircle,
                    onClick = { navController.navigate("attendance") }
                )
                QuickActionPill(
                    title = "PPE Scan",
                    icon = Icons.Default.CameraAlt,
                    onClick = { navController.navigate("ppe-camera") }
                )
                QuickActionPill(
                    title = "Ergonomics",
                    icon = Icons.Default.SelfImprovement,
                    onClick = { navController.navigate("worker-health") }
                )
                QuickActionPill(
                    title = "Report",
                    icon = Icons.Default.ReportProblem,
                    onClick = { navController.navigate("report") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Safety Scans Overview (Donut/Spending Style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.smartfactory.ui.theme.DeepDarkSurface),
                border = BorderStroke(1.dp, com.example.smartfactory.ui.theme.BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Safety Scan Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Chart simulation
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val strokeColor = com.example.smartfactory.ui.theme.TealMint
                            val trackColor = Color(0xFF1E293B)
                            Canvas(modifier = Modifier.size(90.dp)) {
                                drawArc(
                                    color = trackColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    color = strokeColor,
                                    startAngle = -90f,
                                    sweepAngle = 310f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("31", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("scans", fontSize = 10.sp, color = com.example.smartfactory.ui.theme.MutedText)
                            }
                        }

                        // Legends
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f).padding(start = 24.dp)
                        ) {
                            SafetyLegendRow(label = "PPE Passes", percentage = "92%", color = com.example.smartfactory.ui.theme.TealMint)
                            SafetyLegendRow(label = "Safe Posture", percentage = "86%", color = com.example.smartfactory.ui.theme.SoftTealHighlight)
                            SafetyLegendRow(label = "Presence", percentage = "100%", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Operations List
            Text(
                text = "Quick Operations",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OperationRow(
                    title = "AR Assistant Diagnostics",
                    desc = "Inspect sewing machines in AR",
                    icon = Icons.Default.ViewInAr,
                    onClick = { navController.navigate("ar-assistant") }
                )
                OperationRow(
                    title = "My Profile & Status",
                    desc = "Manage credentials & QR pass",
                    icon = Icons.Default.AccountCircle,
                    onClick = { navController.navigate("profile") }
                )
                OperationRow(
                    title = "Emergency Report",
                    desc = "Report a hazard or incident",
                    icon = Icons.Default.ReportProblem,
                    onClick = { navController.navigate("report") }
                )
            }
        }

        // Custom Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.smartfactory.ui.theme.DeepDarkSurface),
                border = BorderStroke(1.dp, com.example.smartfactory.ui.theme.BorderColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(Icons.Default.Home, "Home", tint = com.example.smartfactory.ui.theme.TealMint, modifier = Modifier.size(22.dp))
                        Text("Home", fontSize = 9.sp, color = com.example.smartfactory.ui.theme.TealMint)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate("ppe-camera") }
                    ) {
                        Icon(Icons.Default.CameraAlt, "PPE", tint = Color.Gray, modifier = Modifier.size(22.dp))
                        Text("PPE", fontSize = 9.sp, color = Color.Gray)
                    }

                    // Center spacer for FAB
                    Spacer(modifier = Modifier.size(56.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate("report") }
                    ) {
                        Icon(Icons.Default.ReportProblem, "Report", tint = Color.Gray, modifier = Modifier.size(22.dp))
                        Text("Report", fontSize = 9.sp, color = Color.Gray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate("profile") }
                    ) {
                        Icon(Icons.Default.Person, "Profile", tint = Color.Gray, modifier = Modifier.size(22.dp))
                        Text("Profile", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            // Glowing QR Scanner FAB in Center
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(com.example.smartfactory.ui.theme.TealMint, com.example.smartfactory.ui.theme.EmeraldGlow)
                        )
                    )
                    .clickable { navController.navigate("attendance") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR Attendance Scan",
                    tint = com.example.smartfactory.ui.theme.DeepDarkBg,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionPill(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(com.example.smartfactory.ui.theme.DarkTealPill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = com.example.smartfactory.ui.theme.TealMint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = com.example.smartfactory.ui.theme.MutedText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SafetyLegendRow(label: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, color = com.example.smartfactory.ui.theme.MutedText)
        }
        Text(percentage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun OperationRow(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.smartfactory.ui.theme.DeepDarkSurface),
        border = BorderStroke(1.dp, com.example.smartfactory.ui.theme.BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(com.example.smartfactory.ui.theme.DarkTealPill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = com.example.smartfactory.ui.theme.TealMint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(desc, fontSize = 11.sp, color = com.example.smartfactory.ui.theme.MutedText)
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}