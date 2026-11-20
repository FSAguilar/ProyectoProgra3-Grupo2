package com.example.vamospues.model.estado

import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Participacion
import com.example.vamospues.model.Usuario

sealed interface EstadoLobby {
    fun agregarParticipante(lobby: Lobby, usuario: Usuario): Participacion =
        throw IllegalStateException("No se puede agregar participantes en ${javaClass.simpleName}.")

    fun iniciar(lobby: Lobby): Unit =
        throw IllegalStateException("No se puede iniciar el lobby en ${javaClass.simpleName}.")

    fun finalizar(lobby: Lobby): Unit =
        throw IllegalStateException("No se puede finalizar el lobby en ${javaClass.simpleName}.")

    fun bloquear(lobby: Lobby): Unit =
        throw IllegalStateException("No se puede bloquear el lobby en ${javaClass.simpleName}.")
}
