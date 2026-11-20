package com.example.vamospues.model.estado

import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Participacion
import com.example.vamospues.model.Usuario

class Activo : EstadoLobby {
    override fun agregarParticipante(lobby: Lobby, usuario: Usuario): Participacion =
        lobby.registrarParticipante(usuario)

    override fun iniciar(lobby: Lobby) = lobby.cambiarEstado(EnProceso())
    override fun bloquear(lobby: Lobby) = lobby.cambiarEstado(Bloqueado())
}
