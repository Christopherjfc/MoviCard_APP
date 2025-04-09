package com.example.movicard.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ClienteApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Usar 10.0.2.2 en emulador Android
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClienteApiService::class.java)
    }
}