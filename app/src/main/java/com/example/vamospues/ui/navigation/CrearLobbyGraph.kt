package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.vamospues.model.enums.EstadoAnalisis
import com.example.vamospues.ui.screens.crearlobby.CrearLobbyScreen

fun NavGraphBuilder.crearLobbyGraph(onModeracion: (String) -> Unit) {
    composable(Rutas.CREAR_LOBBY) {
        CrearLobbyScreen(
            onBackClick = {},
            onPublishSuccess = { onModeracion(EstadoAnalisis.PENDIENTE_REVISION.name) }
        )
    }
}
