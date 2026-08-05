/*
 * File: FirebaseDatabaseManager.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.firebase

import com.example.smartfactory.model.SensorData
import com.google.firebase.database.*

/**
 * FirebaseDatabaseManager handles all Realtime Database interactions.
 * It is responsible for establishing a connection to the correct region
 * and safely converting Firebase data into our Kotlin models.
 */
object FirebaseDatabaseManager {

    // 1. Establish connection to our specific regional database (asia-southeast1)
    // 2. Point directly to the "SensorData" folder/node in the database
    private val database =
        FirebaseDatabase.getInstance(
            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
            .reference
            .child("SensorData")

    // Keeps track of our active real-time listener so we can cleanly remove it later
    private var activeListener: ValueEventListener? = null

    /**
     * Attaches a real-time listener to the "SensorData" node.
     * This function fires immediately with current data, and then
     * fires again EVERY TIME the ESP32 pushes new data.
     *
     * @param onDataChanged Callback providing the latest SensorData object
     * @param onError Callback providing an error message if something fails
     */
    fun listenToSensorData(
        onDataChanged: (SensorData) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        // Prevent duplicate listeners by removing any existing one first
        stopListening()

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // We manually map the data instead of using automatic conversion.
                    // This prevents the app from crashing if Firebase sends a whole
                    // number (Long) but our app expects a decimal (Double).
                    val map = snapshot.value as? Map<*, *>
                    
                    if (map != null) {
                        val sensor = SensorData(
                            temperature = (map["temperature"] as? Number)?.toDouble() ?: 0.0,
                            gas = (map["gas"] as? Number)?.toDouble() ?: 0.0,
                            current = (map["current"] as? Number)?.toDouble() ?: 0.0,
                            motion = (map["motion"] as? Number)?.toDouble() ?: 0.0,
                            ldr = (map["ldr"] as? Number)?.toDouble() ?: 0.0,
                            fire = map["fire"] as? Boolean ?: false
                        )
                        onDataChanged(sensor)
                    } else {
                        // The database folder is empty or doesn't exist yet
                        onError("Waiting for ESP32 sensor data...")
                    }
                } catch (e: Exception) {
                    // Catch any unexpected conversion errors gracefully
                    onError("Data format error: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Triggered if Firebase denies read access (e.g., Security Rules)
                onError(error.message)
            }
        }

        // Attach the listener to Firebase
        database.addValueEventListener(listener)
        activeListener = listener
    }

    /**
     * Stops listening for updates.
     * Crucial to call this when the dashboard closes to save battery and data!
     */
    fun stopListening() {
        activeListener?.let { database.removeEventListener(it) }
        activeListener = null
    }
}