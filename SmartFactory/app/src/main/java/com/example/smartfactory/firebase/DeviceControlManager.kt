package com.example.smartfactory.firebase

import com.example.smartfactory.model.DeviceControl
import com.google.firebase.database.*

/**
 * DeviceControlManager handles reading and writing the ON/OFF states
 * of factory machines and relays from Firebase.
 */
object DeviceControlManager {

    private val database =
        FirebaseDatabase.getInstance(
            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
            .reference
            .child("DeviceControl")

    private var activeListener: ValueEventListener? = null

    /**
     * Listens to the current state of devices so the Admin Dashboard switches
     * accurately reflect the actual backend state.
     */
    fun listenToDeviceControl(
        onDataChanged: (DeviceControl) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        stopListening()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val map = snapshot.value as? Map<*, *>
                    if (map != null) {
                        val devices = DeviceControl(
                            lights = map["lights"] as? Boolean ?: false,
                            ac = map["ac"] as? Boolean ?: false,
                            fan = map["fan"] as? Boolean ?: false,
                            pump = map["pump"] as? Boolean ?: false,
                            power = map["power"] as? Boolean ?: true
                        )
                        onDataChanged(devices)
                    } else {
                        // If it's missing, just return the default (all off, power on)
                        onDataChanged(DeviceControl())
                    }
                } catch (e: Exception) {
                    onError("Device format error: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        database.addValueEventListener(listener)
        activeListener = listener
    }

    /**
     * Updates a single device's state in Firebase.
     * The ESP32 will instantly detect this change and switch the physical relay.
     * 
     * @param deviceKey The EXACT key in Firebase (e.g., "lights", "pump")
     * @param isON true to turn ON, false to turn OFF
     */
    fun updateDeviceState(deviceKey: String, isON: Boolean) {
        database.child(deviceKey).setValue(isON)
    }

    fun stopListening() {
        activeListener?.let { database.removeEventListener(it) }
        activeListener = null
    }
}
