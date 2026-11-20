package com.example.vamospues.model

class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val nombreLugar: String,
    val direccion: String,
    val referencia: String,
) {
    fun calcularDistanciaA(otra: Ubicacion): Double {
        // Fase 2: calcular la distancia geográfica en metros.
        return 0.0
    }
}
