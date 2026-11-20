package com.example.vamospues.model.moderacion

import com.example.vamospues.model.Administrador
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.enums.EstadoAnalisis

class FiltroRevisionHumana(
    id: String,
    nombre: String,
    activo: Boolean = true,
    revisanLosAdmins: List<Administrador> = emptyList(),
) : Filtro(id, nombre, activo) {
    private val pendientes = mutableListOf<Lobby>()
    val colaPendientes: List<Lobby> get() = pendientes.toList()

    private val revisores = revisanLosAdmins.toMutableList()
    val revisanLosAdmins: List<Administrador> get() = revisores.toList()

    override fun analizarTexto(texto: String): ResultadoAnalisis =
        ResultadoAnalisis(EstadoAnalisis.PENDIENTE_REVISION, nivelRiesgo = 0)

    fun encolar(lobby: Lobby) {
        if (lobby !in pendientes) pendientes.add(lobby)
    }

    fun resolver(lobby: Lobby, admin: Administrador, decision: EstadoAnalisis, motivo: String): ResultadoAnalisis {
        // Fase 2: verificar permisos y aplicar la decisión al estado del lobby.
        val resultado = ResultadoAnalisis(
            estado = decision,
            nivelRiesgo = 0,
            motivoRechazo = if (decision == EstadoAnalisis.RECHAZADO) motivo else "",
            revisadoPor = admin,
        )
        if (!resultado.estaPendiente()) pendientes.remove(lobby)
        return resultado
    }
}
