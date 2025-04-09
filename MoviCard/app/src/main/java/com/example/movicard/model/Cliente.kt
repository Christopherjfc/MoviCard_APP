package com.example.movicard.model

data class Cliente(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val numero_bloque: Int,
    val numero_piso: Int?,
    val codigopostal: Int,
    val ciudad: String,
    val password: String
)
