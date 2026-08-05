/*
 * File: HealthModels.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.model

data class HealthRequest(
    val image_path: String
)

data class HealthPrediction(
    val posture: String,
    val fatigue: String,
    val status: String,
    val stress: String,
    val facial_state: String = "Unknown"
)
