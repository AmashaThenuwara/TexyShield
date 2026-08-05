/*
 * File: AdminDashboard.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.admin

// ============================================================
// AdminDashboard.kt
// Smart Garment Factory - Industry 4.0
// ============================================================
// Admin portal home screen. Shows:
//   - Live IoT sensor readings in ShinyCards
//   - AI predictions (Fire, Gas, Overcurrent, Maintenance)
//   - Remote device controls (Lights, AC, Fan, Pump, Power)
//   - Emergency report list with resolve capability
//   - Blockchain Ledger button
// All cards use dark theme with glowing blue borders.
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.smartfactory.ai.AIPredictionManager
import com.example.smartfactory.firebase.*
import com.example.smartfactory.model.*
import com.example.smartfactory.ui.components.ShinyButton
import com.example.smartfactory.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminDashboard(navController: NavController, onLogout: () -> Unit = {}) {

    var sensorData by remember { mutableStateOf(SensorData()) }
    var deviceControl by remember { mutableStateOf(DeviceControl()) }
    var currentAlert by remember { mutableStateOf(Alert()) }
    var pendingReports by remember { mutableStateOf(listOf<EmergencyReport>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // AI Prediction states
    var aiFireRisk by remember { mutableStateOf("Connecting...") }
    var aiGasStatus by remember { mutableStateOf("Connecting...") }
    var aiOvercurrentStatus by remember { mutableStateOf("Connecting...") }
    var aiMaintenanceHealth by remember { mutableStateOf(0) }
    var aiMaintenanceStatus by remember { mutableStateOf("Connecting...") }

    var lastScannedWorker by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Listen to all Firebase data sources simultaneously
    DisposableEffect(Unit) {
        FirebaseDatabaseManager.listenToSensorData(
            onDataChanged = { data ->
                sensorData = data
                // Each time sensor data arrives, send it to the AI server
                scope.launch {
                    val fireResult = AIPredictionManager.checkFireRisk(
                        temperature = data.temperature.toDouble(),
                        gas = data.gas.toDouble()
                    )
                    if (fireResult != null) {
                        aiFireRisk = fireResult.risk_level
                    }

                    val gasResult = AIPredictionManager.checkGasAnomaly(data.gas.toDouble())
                    if (gasResult != null) aiGasStatus = gasResult.status

                    val currentResult = AIPredictionManager.checkOvercurrent(data.current.toDouble())
                    if (currentResult != null) aiOvercurrentStatus = currentResult.status

                    val maintResult = AIPredictionManager.checkMachineHealth(
                        air_temperature = 298.1,
                        process_temperature = data.temperature.toDouble() + 273.15,
                        rotational_speed = 1551.0,
                        torque = 42.8,
                        tool_wear = 10.0
                    )
                    if (maintResult != null) {
                        aiMaintenanceHealth = maintResult.health_score
                        aiMaintenanceStatus = maintResult.status
                    }
                }
            },
            onError = { errorMessage = it }
        )

        DeviceControlManager.listenToDeviceControl(
            onDataChanged = { deviceControl = it },
            onError = { if (errorMessage == null) errorMessage = it }
        )

        AlertManager.listenToCurrentAlert(
            onAlertChanged = { currentAlert = it },
            onError = { if (errorMessage == null) errorMessage = it }
        )

        EmergencyReportManager.listenToPendingReports(
            onReportsLoaded = { pendingReports = it },
            onError = { if (errorMessage == null) errorMessage = it }
        )

        // ── ESP32-CAM QR Live Attendance Monitor ─────────────────────────────────────
        // Automatically listens to scans from /AttendanceScans, processes them (mines block + logs permanently),
        // and updates the UI with the last scanned worker's information.
        AttendanceManager.listenAndProcessESP32Scans(
            onProcessed = { message ->
                lastScannedWorker = message
            },
            onMiningCompleted = {},
            onError = { err ->
                if (errorMessage == null) errorMessage = err
            }
        )

        onDispose {
            FirebaseDatabaseManager.stopListening()
            DeviceControlManager.stopListening()
            AlertManager.stopListening()
            AttendanceManager.stopScanListener()
        }
    }

    // Full dark gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(DeepDarkBg, Color(0xFF0F141B))
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Header ────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Smart Garment Factory",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealMint
                        )
                        Text(
                            text = "Admin Control Panel",
                            fontSize = 14.sp,
                            color = MutedText
                        )
                    }
                    // Logout button
                    IconButton(
                        onClick = {
                            navController.navigate("landing") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { navController.navigate("blockchain-ledger") },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(TealMint, EmeraldGlow)),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, null, tint = DeepDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Safety Ledger", color = DeepDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { navController.navigate("attendance-ledger") },
                        modifier = Modifier.weight(1f).height(44.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealMint),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TealMint)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HowToReg, null, tint = TealMint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attendance", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Live ESP32-CAM QR Scanner Status ──────────────────────
            if (lastScannedWorker != null) {
                item {
                    AdminAlertBanner(
                        title = "🟢 ESP32-CAM Scanner Live Scan",
                        message = lastScannedWorker!!,
                        color = Color(0xFF003820),
                        borderColor = Color(0xFF00E676)
                    )
                }
            }

            // ── Error Banner ──────────────────────────────────────────
            if (errorMessage != null) {
                item {
                    AdminAlertBanner(
                        title = "⚠ Error",
                        message = errorMessage!!,
                        color = Color(0xFF4A0010),
                        borderColor = Color(0xFFFF1744)
                    )
                }
            }

            // ── Live Emergency Alert ───────────────────────────────────
            if (currentAlert.type != "NONE" && currentAlert.type.isNotBlank()) {
                item {
                    AdminAlertBanner(
                        title = "🚨 ${currentAlert.type} ALERT",
                        message = "${currentAlert.message} | Level: ${currentAlert.level}",
                        color = Color(0xFF4A0010),
                        borderColor = Color(0xFFFF1744)
                    )
                }
            }

            // ── Pending Emergency Reports ─────────────────────────────
            if (pendingReports.isNotEmpty()) {
                item {
                    SectionHeader(title = "Emergency Reports", icon = Icons.Default.Warning)
                }
                items(pendingReports) { report ->
                    AdminShinyCard(
                        borderColor = Color(0xFFFF1744),
                        content = {
                            Column {
                                Text(
                                    text = "${report.type} — ${report.workerName}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.description,
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ShinyButton(
                                    text = "✔ Resolve Report",
                                    onClick = {
                                        EmergencyReportManager.resolveReport(
                                            reportId = report.id,
                                            onSuccess = {},
                                            onFailure = { errorMessage = it }
                                        )
                                    }
                                )
                            }
                        }
                    )
                }
            }

            // ── Live Sensor Data ──────────────────────────────────────
            item { SectionHeader(title = "Live IoT Sensors", icon = Icons.Default.Info) }
            item {
                AdminShinyCard(borderColor = BorderColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SensorRow("Temperature", "${sensorData.temperature} °C", Icons.Default.Thermostat)
                        SensorRow("Fabric Dust Gas", "${sensorData.gas} ppm", Icons.Default.Cloud)
                        SensorRow("Current", "${sensorData.current} A", Icons.Default.ElectricBolt)
                        SensorRow("Motion Sensor", if (sensorData.motion > 0) "Detected" else "Clear", Icons.Default.DirectionsWalk)
                        SensorRow("Light (LDR)", "${sensorData.ldr}", Icons.Default.Lightbulb)
                    }
                }
            }

            // ── AI Fire Prediction ────────────────────────────────────
            item { SectionHeader(title = "AI Predictions", icon = Icons.Default.SmartToy) }
            item {
                AdminShinyCard(
                    borderColor = if (aiFireRisk == "HIGH") ErrorRed else BorderColor
                ) {
                    AIPredictionRow(
                        title = "Fabric Storage Fire Risk",
                        status = aiFireRisk,
                        isAlert = aiFireRisk == "HIGH",
                        icon = Icons.Default.LocalFireDepartment
                    )
                }
            }
            item {
                AdminShinyCard(
                    borderColor = if (aiGasStatus == "ANOMALY") ErrorRed else BorderColor
                ) {
                    AIPredictionRow(
                        title = "Fabric Dust Accumulation",
                        status = aiGasStatus,
                        isAlert = aiGasStatus == "ANOMALY",
                        icon = Icons.Default.Air
                    )
                }
            }
            item {
                AdminShinyCard(
                    borderColor = if (aiOvercurrentStatus == "OVERCURRENT") ErrorRed else BorderColor
                ) {
                    AIPredictionRow(
                        title = "Sewing Machine Overcurrent",
                        status = aiOvercurrentStatus,
                        isAlert = aiOvercurrentStatus == "OVERCURRENT",
                        icon = Icons.Default.ElectricBolt
                    )
                }
            }
            item {
                AdminShinyCard(
                    borderColor = if (aiMaintenanceStatus != "GOOD") WarningYellow else BorderColor
                ) {
                    Column {
                        AIPredictionRow(
                            title = "Sewing Machine Maintenance",
                            status = aiMaintenanceStatus,
                            isAlert = aiMaintenanceStatus != "GOOD" && aiMaintenanceStatus != "Connecting...",
                            icon = Icons.Default.Build
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { aiMaintenanceHealth / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                aiMaintenanceHealth > 70 -> SuccessGreen
                                aiMaintenanceHealth > 40 -> WarningYellow
                                else -> ErrorRed
                            },
                            trackColor = Color(0xFF1E2A3A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Machine Health: $aiMaintenanceHealth%",
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Device Control ────────────────────────────────────────
            item { SectionHeader(title = "Sewing Floor Device Control", icon = Icons.Default.SettingsRemote) }
            item {
                AdminShinyCard(borderColor = BorderColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DeviceToggleRow("Floor Lights", deviceControl.lights, Icons.Default.Lightbulb) {
                            DeviceControlManager.updateDeviceState("lights", it)
                        }
                        Divider(color = Color(0xFF1E2A3A))
                        DeviceToggleRow("Air Conditioning", deviceControl.ac, Icons.Default.AcUnit) {
                            DeviceControlManager.updateDeviceState("ac", it)
                        }
                        Divider(color = Color(0xFF1E2A3A))
                        DeviceToggleRow("Ventilation Fan", deviceControl.fan, Icons.Default.Air) {
                            DeviceControlManager.updateDeviceState("fan", it)
                        }
                        Divider(color = Color(0xFF1E2A3A))
                        DeviceToggleRow("Steam Iron Pump", deviceControl.pump, Icons.Default.WaterDrop) {
                            DeviceControlManager.updateDeviceState("pump", it)
                        }
                        Divider(color = Color(0xFF1E2A3A))
                        DeviceToggleRow("Main Power", deviceControl.power, Icons.Default.Power) {
                            DeviceControlManager.updateDeviceState("power", it)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ── Reusable Sub-Components ───────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = TealMint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TealMint
        )
    }
}

@Composable
fun AdminShinyCard(
    borderColor: Color = BorderColor,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun AdminAlertBanner(title: String, message: String, color: Color, borderColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = borderColor, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
fun SensorRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = label, color = MutedText, fontSize = 14.sp)
        }
        Text(text = value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun AIPredictionRow(title: String, status: String, isAlert: Boolean, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = title, color = MutedText, fontSize = 14.sp)
        }
        Text(
            text = status,
            color = if (isAlert) ErrorRed else SuccessGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun DeviceToggleRow(label: String, isOn: Boolean, icon: ImageVector? = null, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = label, color = Color.White, fontSize = 14.sp)
        }
        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepDarkBg,
                checkedTrackColor = TealMint,
                uncheckedTrackColor = Color(0xFF1E2A3A)
            )
        )
    }
}
