package com.example.vamospues.ui.screens.admin

import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Reporte
import com.example.vamospues.model.moderacion.Filtro

enum class AdminTab(val etiqueta: String) {
    REPORTES("Reportes"),
    REVISION("Revisión pendiente"),
    FILTROS("Filtros"),
}

sealed interface AdminVista {
    data object Panel : AdminVista
    data class DetalleReporte(val reporteId: String) : AdminVista
    data class RechazoLobby(val lobbyId: String) : AdminVista
}

sealed interface AdminUiState {
    data class Contenido(
        val reportes: List<Reporte>,
        val lobbiesPendientes: List<Lobby>,
        val filtros: List<Filtro>,
    ) : AdminUiState

    data object Cargando : AdminUiState
    data object Error : AdminUiState
}

internal fun contenidoAdmin(
    reportes: List<Reporte>,
    lobbies: List<Lobby>,
    filtros: List<Filtro>,
): AdminUiState.Contenido = AdminUiState.Contenido(
    reportes = reportes,
    lobbiesPendientes = lobbies.filter {
        it.historialAnalisis.lastOrNull()?.estaPendiente() == true
    },
    filtros = filtros,
)
