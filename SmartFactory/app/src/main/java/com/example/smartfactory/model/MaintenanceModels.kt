package com.example.smartfactory.model

data class MaintenanceRequest(
    val temperature: Double,
    val current: Double,
    val vibration: Double,
    val working_hours: Double
)

data class MaintenancePrediction(
    val health_score: Int,
    val status: String
)
