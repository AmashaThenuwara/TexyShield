package com.example.smartfactory.model

// All fields use Double so Firebase JSON numbers (integer or decimal)
// deserialize correctly without returning null.
// Firebase SDK maps numbers to Double by default when using getValue(SensorData::class.java).
data class SensorData(
    val temperature: Double = 0.0,
    val gas: Double = 0.0,
    val current: Double = 0.0,
    val motion: Double = 0.0,
    val ldr: Double = 0.0,
    val fire: Boolean = false
)