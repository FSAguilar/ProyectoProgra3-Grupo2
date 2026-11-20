package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.checkInGraph() {
    composable(
        route = Rutas.CHECKIN,
        arguments = listOf(navArgument(Rutas.ARG_LOBBY_ID) { type = NavType.StringType }),
    ) {
        DestinoProvisional("Check-in")
    }
}

