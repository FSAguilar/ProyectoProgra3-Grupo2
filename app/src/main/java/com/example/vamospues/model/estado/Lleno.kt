package com.example.vamospues.model.estado

import com.example.vamospues.model.Lobby

class Lleno : EstadoLobby {
    override fun iniciar(lobby: Lobby) = lobby.cambiarEstado(EnProceso())
}
