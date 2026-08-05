/*
 * File: AIPredictionManager.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ai


import com.example.smartfactory.api.RetrofitClient
import com.example.smartfactory.model.FireRequest
import com.example.smartfactory.model.FirePrediction



object AIPredictionManager {


    suspend fun checkFireRisk(

        temperature:Double,

        gas:Double

    ): FirePrediction? {


        return try {


            RetrofitClient.api.predictFire(

                FireRequest(

                    temperature,

                    gas

                )

            )


        } catch(e:Exception){


            null


        }

    }

    suspend fun checkGasAnomaly(gas: Double): com.example.smartfactory.model.GasPrediction? {
        return try {
            RetrofitClient.api.detectGasAnomaly(com.example.smartfactory.model.GasRequest(gas))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkOvercurrent(current: Double): com.example.smartfactory.model.CurrentPrediction? {
        return try {
            RetrofitClient.api.detectOvercurrent(com.example.smartfactory.model.CurrentRequest(current))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkMachineHealth(
        air_temperature: Double,
        process_temperature: Double,
        rotational_speed: Double,
        torque: Double,
        tool_wear: Double,
        product_type: String = "M"
    ): com.example.smartfactory.model.MachineHealthPrediction? {
        return try {
            RetrofitClient.api.getMachineHealth(
                com.example.smartfactory.model.MachineHealthRequest(
                    air_temperature, process_temperature, rotational_speed, torque, tool_wear, product_type
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkPPE(
        imagePath: String
    ): com.example.smartfactory.model.PPEPrediction? {
        return try {
            RetrofitClient.api.detectPPE(
                com.example.smartfactory.model.PPERequest(imagePath)
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkWorkerHealth(
        image: String
    ): com.example.smartfactory.model.HealthPrediction? {
        return try {
            RetrofitClient.api.detectWorkerHealth(
                com.example.smartfactory.model.HealthRequest(image)
            )
        } catch (e: Exception) {
            null
        }
    }
}