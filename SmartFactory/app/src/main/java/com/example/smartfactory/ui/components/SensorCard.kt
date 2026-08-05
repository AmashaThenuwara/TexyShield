/*
 * File: SensorCard.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SensorCard(

    title: String,
    value: String,
    status: String

) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.smartfactory.ui.theme.DeepDarkSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.smartfactory.ui.theme.BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = com.example.smartfactory.ui.theme.MutedText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = com.example.smartfactory.ui.theme.LightText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodyLarge,
                color = if (status.contains("Normal", ignoreCase = true) || status.contains("Safe", ignoreCase = true)) {
                    com.example.smartfactory.ui.theme.SuccessGreen
                } else {
                    com.example.smartfactory.ui.theme.ErrorRed
                }
            )
        }
    }

}