package com.example.movicard.model

// Representa una estación de metro
data class Estacion(
    val nombre: String,
    val linea: Linea
)

// Representa una línea de metro
data class Linea(
    val nombre: String,    // Ejemplo: "L1", "L2", "L5"
    val color: String   // Ejemplo: "#FF0000" (Rojo para L1)
)

// recorrido entre dos estaciones
data class PasoEstaciones(
    val estacion: Estacion,
    val tiempoHastaSiguiente: Int // en minutos
)

// estación de transbordo de línea
data class PasoTransbordo(
    val estacionTransbordo: Estacion,
    val nuevaLinea: Linea,
    val tiempoDeEspera: Int
)

// Representa el resumen final
data class TiempoTotal(
    val tiempoTotal: Int // en minutos
)

