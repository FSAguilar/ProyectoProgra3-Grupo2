package com.example.vamospues.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.vamospues.Config
import com.example.vamospues.data.FakeData
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.screens.mislobbies.MisLobbiesScreen
import com.example.vamospues.ui.screens.mislobbies.MisLobbiesTab
import com.example.vamospues.ui.screens.mislobbies.MisLobbiesUiState
import com.example.vamospues.ui.screens.mislobbies.misLobbiesDe

private enum class VarianteMisLobbies(val etiqueta: String) {
    CONTENIDO("Contenido"),
    VACIO("Vacío"),
    CARGANDO("Cargando"),
    ERROR("Error"),
}

fun NavGraphBuilder.misLobbiesGraph(
    onDetalle: (String) -> Unit,
    onExplorar: () -> Unit,
) {
    composable(Rutas.MIS_LOBBIES) { MisLobbiesDestino(onDetalle, onExplorar) }
}

@Composable
private fun MisLobbiesDestino(onDetalle: (String) -> Unit, onExplorar: () -> Unit) {
    val contenido = remember { misLobbiesDe(FakeData.usuarioActual, FakeData.lobbies) }
    var tab by rememberSaveable { mutableStateOf(MisLobbiesTab.CREADOS) }
    var variante by rememberSaveable { mutableStateOf(VarianteMisLobbies.CONTENIDO) }
    val state: MisLobbiesUiState = when (variante) {
        VarianteMisLobbies.CONTENIDO -> contenido
        VarianteMisLobbies.VACIO -> MisLobbiesUiState.Contenido(emptyList(), emptyList())
        VarianteMisLobbies.CARGANDO -> MisLobbiesUiState.Cargando
        VarianteMisLobbies.ERROR -> MisLobbiesUiState.Error
    }

    MisLobbiesScreen(
        state = state,
        tab = tab,
        onTabSelected = { tab = it },
        onLobbyClick = onDetalle,
        onExplorar = onExplorar,
        onReintentar = { variante = VarianteMisLobbies.CONTENIDO },
        selectorDemo = if (Config.MODO_DEMO) {
            {
                DemoEstadoSelector(
                    estados = VarianteMisLobbies.entries.map { it.etiqueta },
                    estadoActual = variante.etiqueta,
                    onEstadoSelected = { etiqueta ->
                        variante = VarianteMisLobbies.entries.first { it.etiqueta == etiqueta }
                    },
                )
            }
        } else null,
    )
}

