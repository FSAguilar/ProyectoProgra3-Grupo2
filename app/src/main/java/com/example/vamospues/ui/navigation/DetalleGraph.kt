package com.example.vamospues.ui.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vamospues.Config
import com.example.vamospues.data.FakeData
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.screens.detalle.AccionDetalle
import com.example.vamospues.ui.screens.detalle.DetalleLobbyScreen
import com.example.vamospues.ui.screens.detalle.DetalleUiState
import com.example.vamospues.ui.screens.detalle.accionPorDefecto
import com.example.vamospues.ui.screens.detalle.detalleLobbyUi
import com.example.vamospues.ui.screens.reportar.EnvioReporte
import com.example.vamospues.ui.screens.reportar.ReportarLobbySheet
import com.example.vamospues.ui.screens.reportar.ReportarUiState
import com.example.vamospues.ui.screens.reportar.VarianteReporte
import com.example.vamospues.ui.screens.reportar.conVariante
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DemoraCargaMs = 850L
private const val DemoraEnvioMs = 1_200L
private const val EtiquetaCargando = "Cargando"
private const val EtiquetaError = "Error"
private const val EtiquetaMenu = "Menú Reportar"

private enum class Carga { LISTO, CARGANDO, ERROR }

/**
 * @param onVolver flecha atrás de la barra superior. Si es null se usa el dispatcher de atrás;
 * NavGraph pasa navigateUp() porque el dispatcher programático no siempre llegaba al NavHost.
 */
fun NavGraphBuilder.detalleGraph(onCheckIn: (String) -> Unit, onVolver: (() -> Unit)? = null) {
    composable(
        route = Rutas.DETALLE,
        arguments = listOf(navArgument(Rutas.ARG_LOBBY_ID) { type = NavType.StringType }),
    ) { entrada ->
        val lobbyId = entrada.arguments?.getString(Rutas.ARG_LOBBY_ID).orEmpty()
        val regreso = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        DetalleDestino(lobbyId, onVolver = onVolver ?: { regreso?.onBackPressed(); Unit }, onCheckIn = onCheckIn)
    }
}

@Composable
private fun DetalleDestino(lobbyId: String, onVolver: () -> Unit, onCheckIn: (String) -> Unit) {
    val usuario = FakeData.usuarioActual
    val lobby = remember(lobbyId) { FakeData.lobbies.firstOrNull { it.id == lobbyId } }
    // En modo demo el evento se presenta como "hoy", igual que en Check-in.
    val datos = remember(lobby) {
        lobby?.let { detalleLobbyUi(it, usuario, if (Config.MODO_DEMO) it.fechaHora.toLocalDate() else LocalDate.now()) }
    }
    val accionInicial = remember(lobby) { lobby?.let { accionPorDefecto(it, usuario) } ?: AccionDetalle.UNIRME }

    // Estado solo de UI: nunca se llama a unirseALobby() ni a reportarLobby() sobre FakeData.
    var accion by rememberSaveable(lobbyId) { mutableStateOf(accionInicial) }
    var unidoDemo by rememberSaveable(lobbyId) { mutableStateOf(false) }
    var carga by rememberSaveable(lobbyId) { mutableStateOf(Carga.LISTO) }
    var reintentando by remember { mutableStateOf(false) }
    var menuAbierto by rememberSaveable(lobbyId) { mutableStateOf(false) }

    var hojaVisible by rememberSaveable(lobbyId) { mutableStateOf(false) }
    var reporte by remember(lobbyId) { mutableStateOf(ReportarUiState()) }
    var envioSimulado by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reintentando) {
        if (reintentando) {
            delay(DemoraCargaMs)
            carga = Carga.LISTO
            reintentando = false
        }
    }

    LaunchedEffect(envioSimulado) {
        if (envioSimulado) {
            delay(DemoraEnvioMs)
            if (reporte.envio == EnvioReporte.ENVIANDO) reporte = reporte.copy(envio = EnvioReporte.ENVIADO)
            envioSimulado = false
        }
    }

    val state = when {
        datos == null -> DetalleUiState.Error(noEncontrado = true)
        carga == Carga.CARGANDO -> DetalleUiState.Cargando
        carga == Carga.ERROR -> DetalleUiState.Error()
        else -> DetalleUiState.Contenido(
            lobby = if (unidoDemo) datos.copy(confirmados = datos.confirmados + 1) else datos,
            accion = accion,
        )
    }

    DetalleLobbyScreen(
        state = state,
        menuAbierto = menuAbierto,
        onMenuAbiertoChange = { menuAbierto = it },
        onVolver = onVolver,
        onReportar = {
            if (reporte.envio == EnvioReporte.ENVIADO) reporte = ReportarUiState()
            hojaVisible = true
        },
        onComoLlegar = {
            scope.launch { snackbar.showSnackbar("Cómo llegar: los mapas no están disponibles en la demo.") }
        },
        onAccion = { seleccionada ->
            when (seleccionada) {
                AccionDetalle.UNIRME -> {
                    if (datos != null && !unidoDemo && datos.cuposDisponibles > 0) unidoDemo = true
                    accion = AccionDetalle.CHECK_IN
                    scope.launch { snackbar.showSnackbar("Te uniste al lobby (demo).") }
                }
                AccionDetalle.SOLICITAR -> {
                    accion = AccionDetalle.PENDIENTE
                    scope.launch { snackbar.showSnackbar("Solicitud enviada al organizador (demo).") }
                }
                AccionDetalle.CHECK_IN -> onCheckIn(lobbyId)
                else -> Unit
            }
        },
        onReintentar = {
            carga = Carga.CARGANDO
            reintentando = true
        },
        snackbarHostState = snackbar,
        selectorDemo = if (Config.MODO_DEMO && datos != null) {
            {
                val etiquetas = AccionDetalle.entries.map { it.etiqueta } +
                    listOf(EtiquetaCargando, EtiquetaError, EtiquetaMenu)
                DemoEstadoSelector(
                    estados = etiquetas,
                    estadoActual = when {
                        carga == Carga.CARGANDO -> EtiquetaCargando
                        carga == Carga.ERROR -> EtiquetaError
                        menuAbierto -> EtiquetaMenu
                        else -> accion.etiqueta
                    },
                    onEstadoSelected = { etiqueta ->
                        reintentando = false
                        when (etiqueta) {
                            EtiquetaCargando -> carga = Carga.CARGANDO
                            EtiquetaError -> carga = Carga.ERROR
                            EtiquetaMenu -> {
                                carga = Carga.LISTO
                                menuAbierto = true
                            }
                            else -> {
                                carga = Carga.LISTO
                                menuAbierto = false
                                accion = AccionDetalle.entries.first { it.etiqueta == etiqueta }
                            }
                        }
                    },
                )
            }
        } else null,
    )

    if (hojaVisible && state is DetalleUiState.Contenido) {
        ReportarLobbySheet(
            state = reporte,
            onMotivoChange = { reporte = reporte.copy(motivo = it) },
            onDetalleChange = { reporte = reporte.copy(detalle = it) },
            onEnviar = {
                if (reporte.puedeEnviar) {
                    reporte = reporte.copy(envio = EnvioReporte.ENVIANDO)
                    envioSimulado = true
                }
            },
            onCerrar = {
                hojaVisible = false
                envioSimulado = false
                // Si falló o se cerró sin enviar, se conserva el borrador para el siguiente intento.
                if (reporte.envio == EnvioReporte.ENVIANDO) reporte = reporte.copy(envio = EnvioReporte.ERROR)
            },
            selectorDemo = if (Config.MODO_DEMO) {
                {
                    DemoEstadoSelector(
                        estados = VarianteReporte.entries.map { it.etiqueta },
                        estadoActual = VarianteReporte.de(reporte).etiqueta,
                        onEstadoSelected = { etiqueta ->
                            envioSimulado = false
                            reporte = reporte.conVariante(VarianteReporte.entries.first { it.etiqueta == etiqueta })
                        },
                    )
                }
            } else null,
        )
    }
}
