package com.example.vamospues.model.estado

import com.example.vamospues.model.Lobby

class PendienteModeracion : EstadoLobby {
    override fun bloquear(lobby: Lobby) = lobby.cambiarEstado(Bloqueado())
}
