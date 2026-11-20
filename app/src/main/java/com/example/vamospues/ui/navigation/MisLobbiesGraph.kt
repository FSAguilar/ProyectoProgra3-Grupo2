package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.misLobbiesGraph() {
    composable(Rutas.MIS_LOBBIES) { DestinoProvisional("Mis lobbies") }
}

