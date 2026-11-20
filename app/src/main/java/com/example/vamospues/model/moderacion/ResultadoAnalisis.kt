package com.example.vamospues.model.moderacion

import com.example.vamospues.model.Administrador
import com.example.vamospues.model.enums.EstadoAnalisis
import java.time.LocalDateTime

class ResultadoAnalisis(
    val estado: EstadoAnalisis,
    val nivelRiesgo: Int,
    val motivoRechazo: String = "",
    val fecha: LocalDateTime = LocalDateTime.now(),
    val revisadoPor: Administrador? = null,
) {
    internal constructor(resultado: ResultadoAnalisis) : this(
        resultado.estado, resultado.nivelRiesgo, resultado.motivoRechazo,
        resultado.fecha, resultado.revisadoPor,
    )

    fun esSeguro(): Boolean = estado == EstadoAnalisis.APROBADO

    fun estaPendiente(): Boolean = estado == EstadoAnalisis.PENDIENTE_REVISION
}
