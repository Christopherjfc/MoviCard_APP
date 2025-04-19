package com.example.movicard.network

import com.example.movicard.model.Cliente
import retrofit2.http.GET
import retrofit2.http.Path

interface ClienteApiService {
    @GET("/api/get/clientes/")
    suspend fun getAllClientes(): List<Cliente>

    @GET("/api/get/clientes/{id}")
    suspend fun getClienteById(@Path("id") id: Int): Cliente
}