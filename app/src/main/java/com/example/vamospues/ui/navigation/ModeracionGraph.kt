package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.moderacionGraph() {
    composable(
        route = Rutas.MODERACION,
        arguments = listOf(navArgument(Rutas.ARG_ESTADO) { type = NavType.StringType }),
    ) {
        DestinoProvisional("Moderación")
    }
}

