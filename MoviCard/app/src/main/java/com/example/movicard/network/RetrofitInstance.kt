package com.example.movicard.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ClienteApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.165.244:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClienteApiService::class.java)
    }
}