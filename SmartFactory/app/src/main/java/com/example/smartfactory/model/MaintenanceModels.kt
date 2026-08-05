/*
 * File: MaintenanceModels.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.model

data class MachineHealthRequest(
    val air_temperature: Double,
    val process_temperature: Double,
    val rotational_speed: Double,
    val torque: Double,
    val tool_wear: Double,
    val product_type: String = "M"
)

data class MachineHealthPrediction(
    val health_score: Int,
    val failure_risk: String,
    val status: String,
    val prediction: String,
    val recommendation: String
)
