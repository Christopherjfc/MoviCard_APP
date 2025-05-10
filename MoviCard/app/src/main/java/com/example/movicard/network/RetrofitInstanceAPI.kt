package com.example.movicard.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceAPI {
    val api: ClienteApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://52.90.60.9:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClienteApiService::class.java)
    }
}