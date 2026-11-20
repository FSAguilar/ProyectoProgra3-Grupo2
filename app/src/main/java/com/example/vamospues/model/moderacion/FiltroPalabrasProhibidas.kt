package com.example.vamospues.model.moderacion

import com.example.vamospues.model.enums.EstadoAnalisis

class FiltroPalabrasProhibidas(
    id: String,
    nombre: String,
    activo: Boolean = true,
) : Filtro(id, nombre, activo) {
    private val palabrasRegistradas = mutableListOf<String>()
    val palabrasProhibidas: List<String> get() = palabrasRegistradas.toList()

    override fun analizarTexto(texto: String): ResultadoAnalisis {
        val palabra = palabrasRegistradas.firstOrNull { texto.contains(it, ignoreCase = true) }
        return if (palabra == null) {
            ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0)
        } else {
            ResultadoAnalisis(EstadoAnalisis.RECHAZADO, nivelRiesgo = 1, motivoRechazo = "Palabra prohibida: $palabra")
        }
    }

    fun agregarPalabra(palabra: String) {
        val limpia = palabra.trim()
        if (limpia.isNotEmpty() && palabrasRegistradas.none { it.equals(limpia, ignoreCase = true) }) {
            palabrasRegistradas.add(limpia)
        }
    }

    fun quitarPalabra(palabra: String) {
        palabrasRegistradas.removeAll { it.equals(palabra.trim(), ignoreCase = true) }
    }
}
