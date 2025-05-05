package com.example.movicard.model

data class TarjetaMoviCard(
    val id: Int,
    val UUID: String?,
    val id_cliente: Int,
    val id_suscripcion: Int,
    val id_ticket: Int?,
    val estadotarjeta: String, // ENUM -> "ACTIVA" o "BLOQUEADA"
    val estadoactivaciontarjeta: String // ENUM -> "ACTIVA" o "DESACTIVADA"
)

