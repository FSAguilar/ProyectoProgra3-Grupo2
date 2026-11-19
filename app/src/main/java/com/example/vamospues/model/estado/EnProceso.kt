package com.example.vamospues.model.estado

import com.example.vamospues.model.Lobby

class EnProceso : EstadoLobby {
    override fun finalizar(lobby: Lobby) = lobby.cambiarEstado(Finalizado())
}
