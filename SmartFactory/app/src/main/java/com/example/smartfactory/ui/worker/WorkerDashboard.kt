package com.example.smartfactory.ui.worker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartfactory.firebase.AlertManager
import com.example.smartfactory.model.Alert

@Composable
fun WorkerDashboard(navController: NavController) {

    var currentAlert by remember { mutableStateOf(Alert()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        AlertManager.listenToCurrentAlert(
            onAlertChanged = { alert ->
                currentAlert = alert
                errorMessage = null
            },
            onError = { err ->
                errorMessage = err
            }
        )
        onDispose {
            AlertManager.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Worker Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Emergency Alert Banner ────────────────────────────────────
        if (currentAlert.type != "NONE" && currentAlert.type.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚨 EMERGENCY ALERT",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = currentAlert.message, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Evacuate Immediately", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✅ Factory Safe",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // ── Navigation Buttons ────────────────────────────────────────

        Button(
            onClick = { navController.navigate("attendance") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Attendance")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { navController.navigate("worker-health") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AI Worker Health")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Profile")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { navController.navigate("report") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Submit Emergency Report")
        }



        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { navController.navigate("ppe-camera") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AI Safety Check")
        }
    }
}