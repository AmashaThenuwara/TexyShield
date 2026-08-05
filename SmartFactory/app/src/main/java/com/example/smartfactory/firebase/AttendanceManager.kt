/*
 * File: AttendanceManager.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.firebase

// =======================================================================
// File: AttendanceManager.kt
// Description: Manages worker attendance database operations.
// Integrates manual attendance logging and live ESP32-CAM QR code scans
// with Firebase Realtime Database and the Blockchain API.
// =======================================================================

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AttendanceManager {

    // Reference to the main "Attendance" node in Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance(
        "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference.child("Attendance")

    private var activeScanListener: ChildEventListener? = null
    private var activeScanQuery: DatabaseReference? = null

    /**
     * Saves attendance manually for the currently logged-in user.
     * Generates dynamic date and time to replace old hardcoded values.
     */
    fun saveAttendance(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onFailure("User not logged in")
            return
        }

        val uid = user.uid
        val currentTimeMs = System.currentTimeMillis()

        // Dynamic date formatting
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDateStr = dateFormatter.format(Date(currentTimeMs))

        val attendance = mapOf(
            "userId" to uid,
            "date" to currentDateStr,
            "time" to currentTimeMs.toString(),
            "status" to "Present"
        )

        // Save into /Attendance/{uid}/{pushId} matching standard Firebase structure
        database.child(uid).push().setValue(attendance)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to log attendance") }
    }

    /**
     * Real-time listener for ESP32-CAM QR scans.
     * When the ESP32 camera scans a worker card and pushes it to /AttendanceScans,
     * this method processes it, writes it to /Attendance/{uid}, mines it to the Blockchain,
     * and deletes the scanned queue item to avoid duplicates.
     */
    fun listenAndProcessESP32Scans(
        onProcessed: (String) -> Unit,
        onMiningCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        stopScanListener()
        val scansRef = FirebaseDatabase.getInstance(
            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference.child("AttendanceScans")

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val scanId = snapshot.key ?: return
                val uid = snapshot.child("uid").value as? String ?: return
                val name = snapshot.child("name").value as? String ?: "Worker"
                val timestampVal = snapshot.child("timestamp").value

                val timestampMs = if (timestampVal is Number) timestampVal.toLong() else System.currentTimeMillis()
                
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val dateStr = dateFormatter.format(Date(timestampMs))
                val timeStr = timeFormatter.format(Date(timestampMs))

                // Play beep sound instantly!
                try {
                    val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
                } catch (e: Exception) {
                    // Ignore sound error
                }

                // Notify UI immediately that the scan occurred (instant feedback)
                onProcessed("Scanned worker: $name")

                // Perform heavy database operations and blockchain mining in background
                val permanentRecord = mapOf(
                    "userId" to uid,
                    "date" to dateStr,
                    "time" to timestampMs.toString(),
                    "status" to "Present"
                )

                database.child(uid).push().setValue(permanentRecord)
                    .addOnSuccessListener {
                        val coroutineScope = CoroutineScope(Dispatchers.IO)
                        coroutineScope.launch {
                            try {
                                val attendanceData = com.example.smartfactory.api.AttendanceData(
                                    worker_uid = uid,
                                    worker_name = name,
                                    timestamp = "$dateStr $timeStr",
                                    shift = "Morning"
                                )
                                com.example.smartfactory.api.RetrofitClient.blockchainApi.mineAttendanceBlock(attendanceData)
                                
                                // Clear the scan queue item
                                scansRef.child(scanId).removeValue()
                                
                                // Notify UI that block is mined and ledger can be reloaded
                                onMiningCompleted()
                            } catch (e: Exception) {
                                onError("Blockchain mining failed: ${e.message}")
                            }
                        }
                    }
                    .addOnFailureListener {
                        onError("Firebase permanent record failed: ${it.message}")
                    }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                onError("Firebase Scan listener cancelled: ${error.message}")
            }
        }

        scansRef.addChildEventListener(listener)
        activeScanListener = listener
        activeScanQuery = scansRef
    }

    /**
     * Removes the scanner child event listener.
     */
    fun stopScanListener() {
        activeScanListener?.let { activeScanQuery?.removeEventListener(it) }
        activeScanListener = null
        activeScanQuery = null
    }
}