package com.example.vamospues.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.compose.composable
import com.example.vamospues.Config

fun NavGraphBuilder.perfilGraph(onAdmin: () -> Unit) {
    composable(Rutas.PERFIL) {
        DestinoProvisional("Perfil") {
            if (Config.MODO_DEMO) {
                TextButton(onClick = onAdmin) { Text("Administración") }
            }
        }
    }
}

