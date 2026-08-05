/*
 * File: RetrofitClient.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.api

// ============================================================
// RetrofitClient.kt
// Smart Garment Factory - Industry 4.0
// ============================================================
// Singleton Retrofit client. Points to the FastAPI AI & Blockchain
// server. IMPORTANT: Change BASE_URL if testing on physical phone.
// ============================================================

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {


    // IMPORTANT: 
    // If testing on ANDROID EMULATOR, use "http://10.0.2.2:8000/"
    // If testing on PHYSICAL PHONE, replace with your PC's IPv4 address e.g. "http://192.168.1.5:8000/"
    const val BASE_URL = "http://172.20.10.14:8000/"  // ← Auto-updated by run_backend.py
    //const val BASE_URL = "http://10.0.2.2:8000/"
    //const val BASE_URL = "http://192.168.43.143:8000/"  // ← Auto-updated by run_backend.py
    val api:AIService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AIService::class.java)

    val blockchainApi: BlockchainApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BlockchainApi::class.java)

}