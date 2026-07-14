package com.example.smartfactory.model

data class PPERequest(
    val image_path: String
)

data class PPEPrediction(
    val detected: List<String>,
    val missing: List<String>,
    val status: String
)
