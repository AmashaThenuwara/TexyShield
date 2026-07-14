package com.example.smartfactory.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {


    private const val BASE_URL =
        "http://192.168.8.172:8000/"


    val api:AIService = Retrofit.Builder()

        .baseUrl(BASE_URL)

        .addConverterFactory(
            GsonConverterFactory.create()
        )

        .build()

        .create(AIService::class.java)

}