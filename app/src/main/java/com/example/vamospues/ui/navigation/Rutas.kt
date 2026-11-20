package com.example.vamospues.ui.navigation

import android.net.Uri

object Rutas {
    const val EXPLORAR = "explorar"
    const val MIS_LOBBIES = "misLobbies"
    const val CHAT = "chat"
    const val PERFIL = "perfil"
    const val CREAR_LOBBY = "crearLobby"
    const val ADMIN = "admin"

    const val ARG_LOBBY_ID = "lobbyId"
    const val ARG_ESTADO = "estado"
    const val DETALLE = "detalle/{$ARG_LOBBY_ID}"
    const val CHECKIN = "checkin/{$ARG_LOBBY_ID}"
    const val MODERACION = "moderacion/{$ARG_ESTADO}"

    fun detalle(lobbyId: String): String = "detalle/${Uri.encode(lobbyId)}"

    fun checkin(lobbyId: String): String = "checkin/${Uri.encode(lobbyId)}"

    fun moderacion(estado: String): String = "moderacion/${Uri.encode(estado)}"
}

