package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.adminGraph() {
    composable(Rutas.ADMIN) { DestinoProvisional("Administración") }
}

