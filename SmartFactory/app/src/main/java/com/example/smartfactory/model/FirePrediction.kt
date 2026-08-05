/*
 * File: FirePrediction.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.model


data class FirePrediction(

    val type:String,

    val risk_level:String,

    val temperature:Double,

    val gas:Double

)