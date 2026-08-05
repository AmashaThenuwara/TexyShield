/*
 * File: AIService.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.api


import com.example.smartfactory.model.FirePrediction
import com.example.smartfactory.model.FireRequest

import retrofit2.http.Body
import retrofit2.http.POST


interface AIService {


    @POST("fire-risk")
    suspend fun predictFire(
        @Body request: FireRequest
    ): FirePrediction

    @POST("gas-anomaly")
    suspend fun detectGasAnomaly(
        @Body request: com.example.smartfactory.model.GasRequest
    ): com.example.smartfactory.model.GasPrediction

    @POST("overcurrent")
    suspend fun detectOvercurrent(
        @Body request: com.example.smartfactory.model.CurrentRequest
    ): com.example.smartfactory.model.CurrentPrediction

    @POST("machine-health")
    suspend fun getMachineHealth(
        @Body request: com.example.smartfactory.model.MachineHealthRequest
    ): com.example.smartfactory.model.MachineHealthPrediction

    @POST("ppe-detection")
    suspend fun detectPPE(
        @Body request: com.example.smartfactory.model.PPERequest
    ): com.example.smartfactory.model.PPEPrediction
    
    @POST("worker-health")
    suspend fun detectWorkerHealth(
        @Body request: com.example.smartfactory.model.HealthRequest
    ): com.example.smartfactory.model.HealthPrediction

    // Machine Type Visual Detection
    // Sends a base64 image to the AI backend and returns the detected machine type
    @POST("machine-type-detection")
    suspend fun detectMachineType(
        @Body request: com.example.smartfactory.model.MachineTypeRequest
    ): com.example.smartfactory.model.MachineTypeResult
}