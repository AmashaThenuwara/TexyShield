/*
 * File: MachineTypeModels.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.model

// ── Machine Type Detection API models ─────────────────────────────────────
// Used by ARAssistantScreen to call POST /machine-type-detection

data class MachineTypeRequest(
    val image_path: String  // base64-encoded image string
)

data class MachineTypeDetection(
    val machine_type: String,
    val confidence: Double
)

data class MachineTypeResult(
    val machine_type: String,
    val confidence: Double,
    val all_detections: List<MachineTypeDetection>,
    val status: String  // "DETECTED" | "NOT_DETECTED" | "MODEL_UNAVAILABLE" | "ERROR"
)
