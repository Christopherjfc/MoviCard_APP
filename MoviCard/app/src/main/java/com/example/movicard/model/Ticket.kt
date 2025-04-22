package com.example.movicard.model

import java.util.Date

data class Ticket(
    val id: Int,
    val tipo: String,
    val cantidad: Int?,
    val duracion_dias: Int?,
    val fecha_inicio: Date?,
    val id_cliente: Int
)
