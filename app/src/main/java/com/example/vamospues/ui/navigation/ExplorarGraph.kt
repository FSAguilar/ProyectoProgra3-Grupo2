package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.compose.composable
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.estado.Activo

fun NavGraphBuilder.explorarGraph(onCrearLobby: () -> Unit, onDetalle: (String) -> Unit) {
    composable(Rutas.EXPLORAR) {
        DestinoProvisional("VamosPues") {
            FakeData.lobbies.filter { it.esPublico() && it.estadoActual is Activo }.forEach { lobby ->
                TextButton(onClick = { onDetalle(lobby.id) }) { Text(lobby.titulo) }
            }
            Button(onClick = onCrearLobby) { Text("Crear lobby") }
        }
    }
}

