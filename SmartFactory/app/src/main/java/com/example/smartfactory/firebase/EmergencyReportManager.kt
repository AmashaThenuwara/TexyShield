package com.example.smartfactory.firebase

import com.example.smartfactory.model.EmergencyReport
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * EmergencyReportManager handles submitting, reading, and resolving
 * manual emergency reports created by workers.
 */
object EmergencyReportManager {

    private val database =
        FirebaseDatabase.getInstance(
            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
            .reference
            .child("EmergencyReports")

    /**
     * Submits a new emergency report to Firebase.
     */
    fun submitReport(
        workerName: String,
        userId: String,
        type: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Generate a unique ID for the report
        val reportId = database.push().key ?: return

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())

        val report = EmergencyReport(
            id = reportId,
            workerName = workerName,
            userId = userId,
            type = type,
            description = description,
            status = "Pending",
            timestamp = currentTime
        )

        database.child(reportId).setValue(report)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.localizedMessage ?: "Failed to submit report") }
    }

    /**
     * Retrieves all pending reports for the admin dashboard.
     * Uses a single read rather than a real-time listener to save data,
     * but can be converted to addValueEventListener if real-time is preferred.
     */
    fun listenToPendingReports(
        onReportsLoaded: (List<EmergencyReport>) -> Unit,
        onError: (String) -> Unit
    ) {
        database.orderByChild("status").equalTo("Pending")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reports = mutableListOf<EmergencyReport>()
                    for (child in snapshot.children) {
                        val report = child.getValue(EmergencyReport::class.java)
                        if (report != null) {
                            reports.add(report)
                        }
                    }
                    // Show newest reports first
                    onReportsLoaded(reports.reversed())
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
    }

    /**
     * Marks a report as resolved.
     */
    fun resolveReport(reportId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        database.child(reportId).child("status").setValue("Resolved")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.localizedMessage ?: "Failed to resolve report") }
    }
}
