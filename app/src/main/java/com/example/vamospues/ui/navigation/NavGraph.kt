package com.example.vamospues.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vamospues.ui.theme.MyApplicationTheme

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        bottomBar = {
            if (destinosInferiores.any { it.ruta == rutaActual }) {
                NavigationBar {
                    destinosInferiores.forEach { destino ->
                        NavigationBarItem(
                            selected = destino.ruta == rutaActual,
                            onClick = {
                                navController.navigate(destino.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                            label = { Text(destino.etiqueta) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.EXPLORAR,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            explorarGraph(
                onCrearLobby = { navController.navigate(Rutas.CREAR_LOBBY) },
                onDetalle = { navController.navigate(Rutas.detalle(it)) },
            )
            misLobbiesGraph()
            chatGraph()
            perfilGraph(onAdmin = { navController.navigate(Rutas.ADMIN) })
            crearLobbyGraph(
                onModeracion = { navController.navigate(Rutas.moderacion(it)) },
                onVolver = { navController.navigateUp() },
            )
            moderacionGraph(
                onVolverExplorar = { navController.popBackStack(Rutas.EXPLORAR, inclusive = false) },
                onVerLobby = { lobbyId ->
                    navController.navigate(Rutas.detalle(lobbyId)) { popUpTo(Rutas.EXPLORAR) }
                },
                onEditarLobby = {
                    if (!navController.popBackStack(Rutas.CREAR_LOBBY, inclusive = false)) {
                        navController.navigate(Rutas.CREAR_LOBBY)
                    }
                },
                onVolver = { navController.navigateUp() },
            )
            detalleGraph(
                onCheckIn = { navController.navigate(Rutas.checkin(it)) },
                onVolver = { navController.navigateUp() },
            )
            checkInGraph()
            adminGraph()
        }
    }
}

@Composable
internal fun DestinoProvisional(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        contenido()
    }
}

private data class DestinoInferior(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val destinosInferiores = listOf(
    DestinoInferior(
        Rutas.EXPLORAR, "Explorar",
        icono("Brújula", "M12,3 A9,9 0,1 1,12 21 A9,9 0,1 1,12 3 Z M15.5,8.5 L13.8,13.8 L8.5,15.5 L10.2,10.2 Z"),
    ),
    DestinoInferior(
        Rutas.MIS_LOBBIES, "Mis lobbies",
        icono("Grupo", "M12,6 A2,2 0,1 1,12 10 A2,2 0,1 1,12 6 Z M5,9 A1.5,1.5 0,1 1,5 12 A1.5,1.5 0,1 1,5 9 Z M19,9 A1.5,1.5 0,1 1,19 12 A1.5,1.5 0,1 1,19 9 Z M7,18 V16 C7,12 17,12 17,16 V18 Z M5,15 C3,15 2,16 2,18 H4 M19,15 C21,15 22,16 22,18 H20"),
    ),
    DestinoInferior(
        Rutas.CHAT, "Chat",
        icono("Chat", "M3,4 H21 V17 H8 L3,21 Z M7,8 H17 M7,11 H17 M7,14 H13"),
    ),
    DestinoInferior(
        Rutas.PERFIL, "Perfil",
        icono("Perfil", "M12,3 A9,9 0,1 1,12 21 A9,9 0,1 1,12 3 Z M12,7 A2.5,2.5 0,1 1,12 12 A2.5,2.5 0,1 1,12 7 Z M6,18 C7,13 17,13 18,18"),
    ),
)

private fun icono(nombre: String, trazo: String): ImageVector =
    ImageVector.Builder(nombre, 24.dp, 24.dp, 24f, 24f)
        .addPath(
            pathData = addPathNodes(trazo),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun NavGraphPreview() {
    MyApplicationTheme { NavGraph() }
}
