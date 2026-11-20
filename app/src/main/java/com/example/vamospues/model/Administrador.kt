package com.example.vamospues.model

import com.example.vamospues.model.enums.EstadoAnalisis
import com.example.vamospues.model.enums.EstadoCuenta
import com.example.vamospues.model.moderacion.Filtro

class Administrador(
    id: String,
    nombre: String,
    email: String,
    val nivelAcceso: Int,
    reputacionKarma: Int = 0,
    estado: EstadoCuenta = EstadoCuenta.ACTIVA,
) : Usuario(id, nombre, email, reputacionKarma, estado) {
    fun banearUsuario(usuario: Usuario) {
        // Fase 2: validar permisos y aplicar el baneo.
    }

    fun eliminarLobby(lobby: Lobby) {
        // Fase 2: validar permisos y eliminar el contenido.
    }

    fun revisarReportesPendientes(): List<Reporte> {
        // Fase 2: consultar los reportes pendientes.
        return emptyList()
    }

    fun configurarFiltro(filtro: Filtro) {
        // Fase 2: aplicar la configuración del filtro.
    }

    fun revisarLobbyPendiente(lobby: Lobby, decision: EstadoAnalisis, motivo: String) {
        // Fase 2: coordinar la resolución con el filtro de revisión humana.
    }
}
