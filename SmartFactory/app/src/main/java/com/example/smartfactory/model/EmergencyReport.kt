package com.example.smartfactory.model

/**
 * Data class representing a manual emergency report submitted by a worker.
 */
data class EmergencyReport(
    val id: String = "", // Used to uniquely identify reports in Firebase
    val workerName: String = "",
    val userId: String = "",
    val type: String = "",
    val description: String = "",
    val status: String = "Pending", // "Pending" or "Resolved"
    val timestamp: String = ""
)
