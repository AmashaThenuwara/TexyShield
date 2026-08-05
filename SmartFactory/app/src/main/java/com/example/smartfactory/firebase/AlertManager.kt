/*
 * File: AlertManager.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.firebase

import com.example.smartfactory.model.Alert
import com.google.firebase.database.*

/**
 * AlertManager listens to the current automated alert from the ESP32.
 */
object AlertManager {

    private val database =
        FirebaseDatabase.getInstance(
            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
            .reference
            .child("Alerts")
            .child("current")

    private var activeListener: ValueEventListener? = null

    /**
     * Listens to the current hardware alert.
     */
    fun listenToCurrentAlert(
        onAlertChanged: (Alert) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        stopListening()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val alert = snapshot.getValue(Alert::class.java)
                    if (alert != null) {
                        onAlertChanged(alert)
                    } else {
                        onAlertChanged(Alert())
                    }
                } catch (e: Exception) {
                    onError("Alert format error: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        database.addValueEventListener(listener)
        activeListener = listener
    }

    fun stopListening() {
        activeListener?.let { database.removeEventListener(it) }
        activeListener = null
    }
}
