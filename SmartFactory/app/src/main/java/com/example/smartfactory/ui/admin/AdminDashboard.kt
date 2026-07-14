package com.example.smartfactory.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.smartfactory.firebase.FirebaseDatabaseManager
import com.example.smartfactory.firebase.DeviceControlManager
import com.example.smartfactory.firebase.AlertManager
import com.example.smartfactory.firebase.EmergencyReportManager

import com.example.smartfactory.model.SensorData
import com.example.smartfactory.model.DeviceControl
import com.example.smartfactory.model.Alert
import com.example.smartfactory.model.EmergencyReport

import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.smartfactory.ai.AIPredictionManager

@Composable
fun AdminDashboard() {

    var sensorData by remember { mutableStateOf(SensorData()) }
    var deviceControl by remember { mutableStateOf(DeviceControl()) }
    var currentAlert by remember { mutableStateOf(Alert()) }
    var pendingReports by remember { mutableStateOf(listOf<EmergencyReport>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var aiRisk by remember {
        mutableStateOf("Waiting...")
    }
    
    var aiTemperature by remember {
        mutableStateOf(0.0)
    }
    
    var aiGas by remember {
        mutableStateOf(0.0)
    }
    
    var aiGasStatus by remember { mutableStateOf("Waiting...") }
    var aiOvercurrentStatus by remember { mutableStateOf("Waiting...") }
    var aiMaintenanceHealth by remember { mutableStateOf(0) }
    var aiMaintenanceStatus by remember { mutableStateOf("Waiting...") }
    
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        FirebaseDatabaseManager.listenToSensorData(
            onDataChanged = { 
                sensorData = it
                
                // Send sensor values to AI
                scope.launch {
                    val fireResult = AIPredictionManager.checkFireRisk(
                        temperature = it.temperature.toDouble(),
                        gas = it.gas.toDouble()
                    )

                    if(fireResult != null){
                        aiRisk = fireResult.risk_level
                        aiTemperature = fireResult.temperature
                        aiGas = fireResult.gas
                    }
                    
                    val gasResult = AIPredictionManager.checkGasAnomaly(it.gas.toDouble())
                    if(gasResult != null){
                        aiGasStatus = gasResult.status
                    }
                    
                    val currentResult = AIPredictionManager.checkOvercurrent(it.current.toDouble())
                    if(currentResult != null){
                        aiOvercurrentStatus = currentResult.status
                    }
                    
                    val maintResult = AIPredictionManager.checkPredictiveMaintenance(
                        temperature = it.temperature.toDouble(),
                        current = it.current.toDouble(),
                        vibration = 5.0, // Assuming static/sensor value
                        workingHours = 120.0 // Assuming static/sensor value
                    )
                    if(maintResult != null){
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

        onDispose {
            FirebaseDatabaseManager.stopListening()
            DeviceControlManager.stopListening()
            AlertManager.stopListening()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {
            Text("Smart Factory Admin", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Emergency Alert Banner ────────────────────────────────────
        if (currentAlert.type != "NONE" && currentAlert.type.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🚨 ${currentAlert.type} ALERT", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                        Text(currentAlert.message, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Level: ${currentAlert.level}", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // ── Emergency Reports ─────────────────────────────────────────
        if (pendingReports.isNotEmpty()) {
            item {
                Text("🚨 Emergency Reports", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(10.dp))
            }
            
            items(pendingReports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${report.type} - ${report.workerName}", style = MaterialTheme.typography.titleMedium)
                        Text(report.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                EmergencyReportManager.resolveReport(
                                    reportId = report.id,
                                    onSuccess = { /* List updates automatically */ },
                                    onFailure = { errorMessage = it }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Resolve")
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // ── Real-time Sensor Values ───────────────────────────────────
        item {
            Text("📊 Live Sensors", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            
            SensorDataRow("🌡", "Temperature", "${sensorData.temperature} °C")
            SensorDataRow("🛢", "Gas Level", "${sensorData.gas}")
            SensorDataRow("⚡", "Current", "${sensorData.current} A")
            SensorDataRow("🚶", "Motion", if (sensorData.motion > 0) "Detected" else "Clear")
            SensorDataRow("💡", "Light (LDR)", "${sensorData.ldr}")
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── AI Fire Prediction ───────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if(aiRisk == "HIGH")
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ){
                Column(
                    modifier = Modifier.padding(16.dp)
                ){
                    Text(
                        "🤖 AI Fire Prediction",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    Text(
                        "Risk Level : $aiRisk"
                    )
                    Text(
                        "Temperature : $aiTemperature °C"
                    )
                    Text(
                        "Gas Level : $aiGas"
                    )
                }
            }
        }
        
        // ── AI Gas Anomaly Detection ───────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if(aiGasStatus == "ANOMALY")
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ){
                Column(
                    modifier = Modifier.padding(16.dp)
                ){
                    Text(
                        "🤖 AI Gas Anomaly",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Status : $aiGasStatus")
                }
            }
        }

        // ── AI Overcurrent Detection ───────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if(aiOvercurrentStatus == "OVERCURRENT")
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ){
                Column(
                    modifier = Modifier.padding(16.dp)
                ){
                    Text(
                        "🤖 AI Overcurrent Detection",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Status : $aiOvercurrentStatus")
                }
            }
        }

        // ── AI Predictive Maintenance ───────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if(aiMaintenanceStatus != "GOOD" && aiMaintenanceStatus != "Waiting...")
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ){
                Column(
                    modifier = Modifier.padding(16.dp)
                ){
                    Text(
                        "🤖 AI Predictive Maintenance",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Machine Health : $aiMaintenanceHealth%")
                    Text("Status : $aiMaintenanceStatus")
                }
            }
        }


        // ── Device Control Switches ───────────────────────────────────
        item {
            Text("🎛 Device Control", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            
            DeviceSwitchRow("💡", "Factory Lights", deviceControl.lights) { DeviceControlManager.updateDeviceState("lights", it) }
            DeviceSwitchRow("❄️", "Air Conditioning", deviceControl.ac) { DeviceControlManager.updateDeviceState("ac", it) }
            DeviceSwitchRow("🌀", "Ventilation Fan", deviceControl.fan) { DeviceControlManager.updateDeviceState("fan", it) }
            DeviceSwitchRow("💧", "Water Pump", deviceControl.pump) { DeviceControlManager.updateDeviceState("pump", it) }
            DeviceSwitchRow("⚡", "Main Power", deviceControl.power) { DeviceControlManager.updateDeviceState("power", it) }
        }
    }
}

@Composable
fun SensorDataRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text("$icon $label : ", style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun DeviceSwitchRow(icon: String, label: String, isON: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$icon $label", style = MaterialTheme.typography.bodyLarge)
        Switch(checked = isON, onCheckedChange = onToggle)
    }
}
