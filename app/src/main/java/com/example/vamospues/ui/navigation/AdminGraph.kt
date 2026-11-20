package com.example.vamospues.ui.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import com.example.vamospues.model.enums.AccionModeracion
import com.example.vamospues.model.enums.EstadoReporte
import com.example.vamospues.model.moderacion.FiltroPalabrasProhibidas
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.screens.admin.AdminScreen
import com.example.vamospues.ui.screens.admin.AdminTab
import com.example.vamospues.ui.screens.admin.AdminUiState
import com.example.vamospues.ui.screens.admin.AdminVista
import com.example.vamospues.ui.screens.admin.contenidoAdmin
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private enum class VarianteAdmin(val etiqueta: String) {
    DATOS("Datos"),
    VACIO("Vacío"),
    CARGANDO("Cargando"),
    ERROR("Error"),
}

fun NavGraphBuilder.adminGraph() {
    composable(Rutas.ADMIN) { AdminDestino() }
}

@Composable
private fun AdminDestino() {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val contenidoBase = remember {
        contenidoAdmin(FakeData.reportes, FakeData.lobbies, FakeData.filtros)
    }
    var variante by rememberSaveable { mutableStateOf(VarianteAdmin.DATOS) }
    var tab by rememberSaveable { mutableStateOf(AdminTab.REPORTES) }
    var vista by remember { mutableStateOf<AdminVista>(AdminVista.Panel) }
    var estadosReportes by remember { mutableStateOf(emptyMap<String, EstadoReporte>()) }
    var lobbiesRevisados by remember { mutableStateOf(emptySet<String>()) }
    var filtrosActivos by remember {
        mutableStateOf(FakeData.filtros.associate { it.id to it.estaActivo() })
    }
    val palabrasIniciales = remember {
        FakeData.filtros.filterIsInstance<FiltroPalabrasProhibidas>()
            .firstOrNull()?.palabrasProhibidas?.joinToString("\n").orEmpty()
    }
    var palabras by rememberSaveable { mutableStateOf(palabrasIniciales) }
    var palabrasGuardadas by rememberSaveable { mutableStateOf(palabrasIniciales) }
    var motivoRechazo by rememberSaveable { mutableStateOf("") }
    var reintentando by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(reintentando) {
        if (reintentando) {
            delay(700.milliseconds)
            variante = VarianteAdmin.DATOS
            reintentando = false
        }
    }

    val contenido = contenidoBase.copy(
        lobbiesPendientes = contenidoBase.lobbiesPendientes.filterNot { it.id in lobbiesRevisados },
    )
    val state: AdminUiState = when (variante) {
        VarianteAdmin.DATOS -> contenido
        VarianteAdmin.VACIO -> AdminUiState.Contenido(emptyList(), emptyList(), emptyList())
        VarianteAdmin.CARGANDO -> AdminUiState.Cargando
        VarianteAdmin.ERROR -> AdminUiState.Error
    }

    AdminScreen(
        state = state,
        administrador = FakeData.administrador,
        tab = tab,
        vista = vista,
        estadosReportes = estadosReportes,
        filtrosActivos = filtrosActivos,
        palabras = palabras,
        palabrasModificadas = palabras != palabrasGuardadas,
        motivoRechazo = motivoRechazo,
        onTabSelected = {
            tab = it
            vista = AdminVista.Panel
            motivoRechazo = ""
        },
        onBack = {
            if (vista == AdminVista.Panel) backDispatcher?.onBackPressed()
            else {
                vista = AdminVista.Panel
                motivoRechazo = ""
            }
        },
        onReporteSelected = { vista = AdminVista.DetalleReporte(it) },
        onAccionReporte = { reporteId, accion ->
            estadosReportes = estadosReportes + (
                reporteId to if (accion == AccionModeracion.DESCARTAR) {
                    EstadoReporte.DESCARTADO
                } else {
                    EstadoReporte.RESUELTO
                }
            )
            vista = AdminVista.Panel
        },
        onAprobarLobby = { lobbiesRevisados = lobbiesRevisados + it },
        onRechazarLobby = {
            motivoRechazo = ""
            vista = AdminVista.RechazoLobby(it)
        },
        onMotivoRechazoChange = { motivoRechazo = it },
        onConfirmarRechazo = {
            if (motivoRechazo.isNotBlank()) {
                lobbiesRevisados = lobbiesRevisados + it
                motivoRechazo = ""
                vista = AdminVista.Panel
            }
        },
        onCancelarRechazo = {
            motivoRechazo = ""
            vista = AdminVista.Panel
        },
        onFiltroActivoChange = { id, activo -> filtrosActivos = filtrosActivos + (id to activo) },
        onPalabrasChange = { palabras = it },
        onGuardarPalabras = { palabrasGuardadas = palabras },
        onReintentar = {
            variante = VarianteAdmin.CARGANDO
            reintentando = true
        },
        selectorDemo = if (Config.MODO_DEMO) {
            {
                DemoEstadoSelector(
                    estados = VarianteAdmin.entries.map { it.etiqueta },
                    estadoActual = variante.etiqueta,
                    onEstadoSelected = { etiqueta ->
                        reintentando = false
                        variante = VarianteAdmin.entries.first { it.etiqueta == etiqueta }
                        vista = AdminVista.Panel
                    },
                )
            }
        } else null,
    )
}

