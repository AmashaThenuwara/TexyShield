package com.example.smartfactory.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.smartfactory.firebase.FirebaseDatabaseManager
import com.example.smartfactory.model.SensorData

@Composable
fun AdminDashboard() {

    // Holds the latest sensor data received from Firebase
    var sensorData by remember { mutableStateOf(SensorData()) }

    // Holds any error message to show if Firebase fails
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // DisposableEffect runs when the screen appears AND cleans up when it disappears.
    // This is the correct Compose lifecycle hook for real-time listeners.
    DisposableEffect(Unit) {

        // Start listening — fires immediately with current data, then on every change
        FirebaseDatabaseManager.listenToSensorData(
            onDataChanged = { newData ->
                sensorData = newData
                errorMessage = null   // clear any previous error
            },
            onError = { message ->
                errorMessage = message
            }
        )

        // Called automatically when this screen is removed from composition
        onDispose {
            FirebaseDatabaseManager.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Show error banner if Firebase connection fails
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
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

        // ── Sensor Readings ─────────────────────────────────────────────

        Text("🌡 Temperature : ${sensorData.temperature} °C")

        Spacer(modifier = Modifier.height(10.dp))

        Text("🛢 Gas : ${sensorData.gas}")

        Spacer(modifier = Modifier.height(10.dp))

        Text("⚡ Current : ${sensorData.current}")

        Spacer(modifier = Modifier.height(10.dp))

        Text("🚶 Motion : ${sensorData.motion}")

        Spacer(modifier = Modifier.height(10.dp))

        Text("💡 LDR : ${sensorData.ldr}")

        Spacer(modifier = Modifier.height(20.dp))

        // ── Fire Alert ──────────────────────────────────────────────────

        if (sensorData.fire) {
            Text(
                text = "🔥 FIRE ALERT",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineSmall
            )
        } else {
            Text("✅ Factory Safe")
        }
    }
}