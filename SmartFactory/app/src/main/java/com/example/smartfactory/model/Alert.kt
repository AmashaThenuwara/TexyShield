package com.example.smartfactory.model

/**
 * Data class representing automated hardware alerts from the ESP32.
 * type: FIRE, TEMPERATURE, GAS, POWER, or NONE
 * level: DANGER, WARNING, CRITICAL
 */
data class Alert(
    val type: String = "NONE",
    val message: String = "",
    val level: String = "",
    val timestamp: String = ""
)
