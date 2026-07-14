package com.example.smartfactory.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartfactory.firebase.FirebaseAuthManager

/**
 * LoginScreen displays the authentication form.
 * @param onLoginSuccess Function to call when authentication passes,
 *                       allowing the main app to switch screens.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    // Input field states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // UI Feedback states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Smart Factory Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Password Input Field (masks text automatically)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login Button
        Button(
            onClick = {
                // 1. Reset UI to loading state
                errorMessage = null
                isLoading = true

                // 2. Call our Firebase Auth Manager
                FirebaseAuthManager.login(
                    email = email.trim(),
                    password = password,
                    onSuccess = {
                        isLoading = false
                        onLoginSuccess()   // Triggers navigation to Dashboard
                    },
                    onFailure = { message ->
                        isLoading = false
                        errorMessage = message // Displays error to user
                    }
                )
            },
            enabled = !isLoading, // Disable button while loading
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show a loading spinner if processing, otherwise show text
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        // Display error message at the bottom if authentication fails
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
