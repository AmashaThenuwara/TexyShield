package com.example.smartfactory.model

/**
 * Data class representing the state of factory devices.
 * True = ON, False = OFF.
 */
data class DeviceControl(
    val lights: Boolean = false,
    val ac: Boolean = false,
    val fan: Boolean = false,
    val pump: Boolean = false,
    val power: Boolean = true // Power defaults to ON
)
