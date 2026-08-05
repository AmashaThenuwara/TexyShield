/*
 * File: CurrentModels.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.model

data class CurrentRequest(
    val current: Double
)

data class CurrentPrediction(
    val status: String
)
