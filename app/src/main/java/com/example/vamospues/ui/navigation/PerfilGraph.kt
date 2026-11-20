package com.example.vamospues.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.vamospues.ui.screens.perfil.PerfilScreen
import com.example.vamospues.ui.screens.perfil.PerfilUiState
import com.example.vamospues.ui.screens.perfil.perfilDe
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private enum class VariantePerfil(val etiqueta: String) {
    ACTIVA("Activa"),
    SIN_HISTORIAL("Sin historial"),
    SUSPENDIDA("Suspendida"),
    BANEADA("Baneada"),
    CARGANDO("Cargando"),
    ERROR("Error"),
    ADMIN("Admin"),
}

fun NavGraphBuilder.perfilGraph(onAdmin: () -> Unit) {
    composable(Rutas.PERFIL) { PerfilDestino(onAdmin) }
}

@Composable
private fun PerfilDestino(onAdmin: () -> Unit) {
    var variante by rememberSaveable { mutableStateOf(VariantePerfil.ACTIVA) }
    var ultimoContenido by rememberSaveable { mutableStateOf(VariantePerfil.ACTIVA) }
    var reintentando by rememberSaveable { mutableStateOf(false) }
    val state = remember(variante) {
        when (variante) {
            VariantePerfil.ACTIVA -> perfilDe(FakeData.usuarioActual)
            VariantePerfil.SIN_HISTORIAL -> PerfilUiState.Contenido(FakeData.usuarioActual, emptyList())
            VariantePerfil.SUSPENDIDA -> perfilDe(FakeData.usuarioSuspendido)
            VariantePerfil.BANEADA -> perfilDe(FakeData.usuarioBaneado)
            VariantePerfil.CARGANDO -> PerfilUiState.Cargando
            VariantePerfil.ERROR -> PerfilUiState.Error
            VariantePerfil.ADMIN -> perfilDe(FakeData.administrador)
        }
    }

    LaunchedEffect(reintentando) {
        if (reintentando) {
            delay(850.milliseconds)
            variante = ultimoContenido
            reintentando = false
        }
    }

    PerfilScreen(
        state = state,
        onReintentar = {
            variante = VariantePerfil.CARGANDO
            reintentando = true
        },
        onAdmin = onAdmin.takeIf { Config.MODO_DEMO && variante == VariantePerfil.ADMIN },
        selectorDemo = if (Config.MODO_DEMO) {
            {
                DemoEstadoSelector(
                    estados = VariantePerfil.entries.map { it.etiqueta },
                    estadoActual = variante.etiqueta,
                    onEstadoSelected = { etiqueta ->
                        reintentando = false
                        val seleccionada = VariantePerfil.entries.first { it.etiqueta == etiqueta }
                        if (seleccionada != VariantePerfil.CARGANDO && seleccionada != VariantePerfil.ERROR) {
                            ultimoContenido = seleccionada
                        }
                        variante = seleccionada
                    },
                )
            }
        } else null,
    )
}
