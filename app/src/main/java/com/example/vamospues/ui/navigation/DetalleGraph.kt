package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vamospues.Config
import com.example.vamospues.data.FakeData

fun NavGraphBuilder.detalleGraph(onCheckIn: (String) -> Unit) {
    composable(
        route = Rutas.DETALLE,
        arguments = listOf(navArgument(Rutas.ARG_LOBBY_ID) { type = NavType.StringType }),
    ) { entrada ->
        val lobbyId = entrada.arguments?.getString(Rutas.ARG_LOBBY_ID)
        val lobby = FakeData.lobbies.firstOrNull { it.id == lobbyId }
        DestinoProvisional(lobby?.titulo ?: "Detalle") {
            if (Config.MODO_DEMO && lobby != null) {
                TextButton(onClick = { onCheckIn(lobby.id) }) { Text("Check-in") }
            }
        }
    }
}

