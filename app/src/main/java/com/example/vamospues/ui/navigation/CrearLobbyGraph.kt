package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.compose.composable
import com.example.vamospues.Config
import com.example.vamospues.model.enums.EstadoAnalisis

fun NavGraphBuilder.crearLobbyGraph(onModeracion: (String) -> Unit) {
    composable(Rutas.CREAR_LOBBY) {
        DestinoProvisional("Crear lobby") {
            if (Config.MODO_DEMO) {
                Button(onClick = { onModeracion(EstadoAnalisis.PENDIENTE_REVISION.name) }) {
                    Text("Enviar a moderación")
                }
            }
        }
    }
}

