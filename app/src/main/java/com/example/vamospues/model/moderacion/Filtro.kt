package com.example.vamospues.model.moderacion

abstract class Filtro(
    val id: String,
    val nombre: String,
    activo: Boolean = true,
) {
    var activo: Boolean = activo
        private set

    abstract fun analizarTexto(texto: String): ResultadoAnalisis

    fun estaActivo(): Boolean = activo
}
