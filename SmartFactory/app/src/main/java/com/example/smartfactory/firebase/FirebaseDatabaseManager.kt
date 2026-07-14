package com.example.smartfactory.firebase

import com.example.smartfactory.model.SensorData
import com.google.firebase.database.*

object FirebaseDatabaseManager {

    // Explicitly pass the asia-southeast1 regional URL.
    // Without this, Firebase SDK defaults to us-central1 and cannot find the database.
    private val database =
        FirebaseDatabase.getInstance(
            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
            .reference
            .child("SensorData")

    // Keeps a reference to the active listener so we can remove it later if needed
    private var activeListener: ValueEventListener? = null

    // Attaches a real-time listener that fires every time Firebase data changes.
    // Replaces any previous listener to avoid duplicates.
    fun listenToSensorData(
        onDataChanged: (SensorData) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        // Remove existing listener before attaching a new one
        activeListener?.let { database.removeEventListener(it) }

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Safe manual mapping to handle both Long and Double from Firebase
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
                        // Data doesn't exist or isn't a map
                        onError("Waiting for sensor data...")
                    }
                } catch (e: Exception) {
                    onError("Data conversion error: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        database.addValueEventListener(listener)
        activeListener = listener
    }

    // Call this when the screen leaves composition to stop receiving updates
    fun stopListening() {
        activeListener?.let { database.removeEventListener(it) }
        activeListener = null
    }
}

// ────── How this works ──────
// Firebase.getInstance(url)    → connects to the correct regional database
// .child("SensorData")         → points to the /SensorData node in your JSON tree
// addValueEventListener(...)   → fires IMMEDIATELY with current data, then fires
//                                again every time ESP32 writes a new value
// onDataChange(snapshot)       → called on the MAIN thread by the Firebase SDK,
//                                so it's safe to update Compose state directly