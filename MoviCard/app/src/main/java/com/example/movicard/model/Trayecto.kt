package com.example.movicard.model

data class Trayecto(
    val estacion: Estacion,
    val linea: Linea,
    val ruta: MutableList<Estacion>,
    val transbordos: MutableList<PasoTransbordo>
)
