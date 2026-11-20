package com.example.vamospues.ui.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vamospues.ui.screens.moderacion.ResultadoPublicacionScreen

fun NavGraphBuilder.moderacionGraph(
    onVolverExplorar: () -> Unit = {},
    onVerLobby: (String) -> Unit = {},
    onEditarLobby: (String) -> Unit = {},
    onVolver: (() -> Unit)? = null,
) {
    composable(
        route = Rutas.MODERACION,
        arguments = listOf(navArgument(Rutas.ARG_ESTADO) { type = NavType.StringType }),
    ) { entrada ->
        val estado = entrada.arguments?.getString(Rutas.ARG_ESTADO) ?: "PENDIENTE_REVISION"
        val regreso = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        ResultadoPublicacionScreen(
            estadoInicial = estado,
            onVolverExplorar = onVolverExplorar,
            onVerLobby = onVerLobby,
            onEditarLobby = onEditarLobby,
            onBackClick = onVolver ?: { regreso?.onBackPressed(); Unit }
        )
    }
}
