package com.example.vamospues.model

import com.example.vamospues.model.enums.AccionModeracion
import com.example.vamospues.model.enums.EstadoReporte
import java.time.LocalDateTime

class Reporte(
    val id: String,
    val motivo: String,
    val reportado: Reportable,
    val fecha: LocalDateTime = LocalDateTime.now(),
    estado: EstadoReporte = EstadoReporte.PENDIENTE,
    val revisadoPor: Administrador? = null,
) {
    var estado: EstadoReporte = estado
        private set

    fun resolver(accion: AccionModeracion) {
        estado = if (accion == AccionModeracion.DESCARTAR) {
            EstadoReporte.DESCARTADO
        } else {
            EstadoReporte.RESUELTO
        }
        // Fase 2: aplicar la acción de moderación.
    }
}
