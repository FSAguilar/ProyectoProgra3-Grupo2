package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.vamospues.ui.screens.explorar.ExplorarScreen

fun NavGraphBuilder.explorarGraph(onCrearLobby: () -> Unit, onDetalle: (String) -> Unit) {
    composable(Rutas.EXPLORAR) {
        ExplorarScreen(
            onLobbyClick = onDetalle,
            onCreateLobbyClick = onCrearLobby
        )
    }
}
