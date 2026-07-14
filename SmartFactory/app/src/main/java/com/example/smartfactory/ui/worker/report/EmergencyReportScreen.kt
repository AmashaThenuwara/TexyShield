package com.example.smartfactory.ui.worker.report

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartfactory.firebase.EmergencyReportManager
import com.example.smartfactory.firebase.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyReportScreen(
    onNavigateBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf("Fire") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDropdown by remember { mutableStateOf(false) }

    val reportTypes = listOf(
        "🔥 Fire",
        "🤕 Injury",
        "⚡ Electrical Fault",
        "🛢 Gas Leak",
        "🏭 Machine Failure",
        "🚑 Medical Emergency"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "🚨 Emergency Report",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Report Type")
        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = { showDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                reportTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            showDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Description")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Describe the emergency details here...") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    errorMessage = "You must be logged in to report."
                    return@Button
                }
                
                isLoading = true
                errorMessage = null

                EmergencyReportManager.submitReport(
                    workerName = user.email ?: "Unknown Worker", // Real app would use profile name
                    userId = user.uid,
                    type = selectedType,
                    description = description,
                    onSuccess = {
                        isLoading = false
                        onNavigateBack() // Go back to dashboard on success
                    },
                    onFailure = {
                        isLoading = false
                        errorMessage = it
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && description.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onError)
            } else {
                Text("Submit Report")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}
