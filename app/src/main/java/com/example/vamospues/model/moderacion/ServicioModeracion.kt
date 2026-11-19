package com.example.vamospues.model.moderacion

import com.example.vamospues.model.Administrador
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Reporte
import com.example.vamospues.model.enums.AccionModeracion
import com.example.vamospues.model.enums.EstadoAnalisis

class ServicioModeracion(filtro: Filtro, vararg filtrosAdicionales: Filtro) {
    private val filtrosRegistrados = mutableListOf(filtro, *filtrosAdicionales)
    val filtros: List<Filtro> get() = filtrosRegistrados.toList()

    fun evaluarLobby(lobby: Lobby): ResultadoAnalisis {
        val activos = filtrosRegistrados.filter { it.estaActivo() }
        val resultados = activos.map { it.analizarTexto(lobby.textoParaModeracion()) }
        val resultado = resultados.firstOrNull { !it.esSeguro() && !it.estaPendiente() }
            ?: resultados.firstOrNull { it.estaPendiente() }
            ?: resultados.firstOrNull()
            ?: ResultadoAnalisis(EstadoAnalisis.PENDIENTE_REVISION, nivelRiesgo = 0)
        if (resultado.estaPendiente()) activos.filterIsInstance<FiltroRevisionHumana>().forEach { it.encolar(lobby) }
        return resultado
    }

    fun procesarReporte(reporte: Reporte, admin: Administrador, accion: AccionModeracion) {
        // Fase 2: verificar permisos, registrar el revisor y aplicar la acción.
        reporte.resolver(accion)
    }

    fun agregarFiltro(filtro: Filtro) {
        filtrosRegistrados.add(filtro)
    }
}
