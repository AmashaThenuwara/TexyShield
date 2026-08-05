/*
 * File: EmergencyReportScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import com.example.smartfactory.firebase.EmergencyReportManager
import com.example.smartfactory.firebase.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    val reportTypes = listOf(
        "Fabric Storage Fire",
        "Needle Injury",
        "Electrical Fault",
        "Fabric Dust Accumulation",
        "Sewing Machine Failure",
        "Thread/Fabric Shortage",
        "Medical Emergency"
    )

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(DeepDarkBg, Color(0xFF0C1014))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFFF1744),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Emergency Report",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF1744)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Report Type", color = TealMint, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = { showDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TealMint,
                    unfocusedBorderColor = MutedText
                )
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

        Spacer(modifier = Modifier.height(24.dp))

        Text("Description", color = TealMint, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Describe the emergency details here...", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = TealMint,
                unfocusedBorderColor = MutedText
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                        coroutineScope.launch {
                            try {
                                val reportData = com.example.smartfactory.api.ReportData(
                                    worker_id = user.uid,
                                    issue = selectedType,
                                    location = "Factory Floor",
                                    timestamp = System.currentTimeMillis().toString()
                                )
                                com.example.smartfactory.api.RetrofitClient.blockchainApi.mineBlock(reportData)
                            } catch (e: Exception) {
                                // Just log or ignore for now if blockchain fails
                            }
                            isLoading = false
                            onNavigateBack() // Go back to dashboard on success
                        }
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
            Text(text = errorMessage!!, color = Color(0xFFFF1744))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = TealMint)
        }
    }
}
}
