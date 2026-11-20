package com.example.vamospues.ui.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.vamospues.model.enums.EstadoAnalisis
import com.example.vamospues.ui.screens.crearlobby.CrearLobbyScreen

fun NavGraphBuilder.crearLobbyGraph(onModeracion: (String) -> Unit, onVolver: (() -> Unit)? = null) {
    composable(Rutas.CREAR_LOBBY) {
        val regreso = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        CrearLobbyScreen(
            onBackClick = onVolver ?: { regreso?.onBackPressed(); Unit },
            onPublishSuccess = { onModeracion(EstadoAnalisis.PENDIENTE_REVISION.name) }
        )
    }
}
