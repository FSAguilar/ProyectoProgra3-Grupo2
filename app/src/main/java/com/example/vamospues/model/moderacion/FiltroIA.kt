package com.example.vamospues.model.moderacion

import com.example.vamospues.model.enums.EstadoAnalisis

class FiltroIA(
    id: String,
    nombre: String,
    private val apiEndpoint: String,
    private val umbralSensibilidad: Float,
    activo: Boolean = true,
) : Filtro(id, nombre, activo) {
    override fun analizarTexto(texto: String): ResultadoAnalisis {
        // Fase 2: conectar el servicio de análisis.
        return ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0)
    }
}
