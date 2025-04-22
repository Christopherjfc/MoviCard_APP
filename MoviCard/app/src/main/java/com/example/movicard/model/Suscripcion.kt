package com.example.movicard.model

data class Suscripcion (
    val id: Int,
    val suscripcion: String, // "GRATUITA" o "PREMIUM"
    val id_cliente: Int
)