package com.example.movicard

import com.example.movicard.network.InoviceURLService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.56.3:3000/"

    val instance: InoviceURLService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InoviceURLService::class.java)
    }
}