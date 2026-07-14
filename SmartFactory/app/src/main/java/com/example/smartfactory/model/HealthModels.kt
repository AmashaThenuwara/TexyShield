package com.example.smartfactory.model

data class HealthRequest(
    val image_path: String
)

data class HealthPrediction(
    val posture: String,
    val fatigue: String,
    val status: String
)
