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

    @POST("predictive-maintenance")
    suspend fun getPredictiveMaintenance(
        @Body request: com.example.smartfactory.model.MaintenanceRequest
    ): com.example.smartfactory.model.MaintenancePrediction

    @POST("ppe-detection")
    suspend fun detectPPE(
        @Body request: com.example.smartfactory.model.PPERequest
    ): com.example.smartfactory.model.PPEPrediction
    
    @POST("worker-health")
    suspend fun detectWorkerHealth(
        @Body request: com.example.smartfactory.model.HealthRequest
    ): com.example.smartfactory.model.HealthPrediction
}