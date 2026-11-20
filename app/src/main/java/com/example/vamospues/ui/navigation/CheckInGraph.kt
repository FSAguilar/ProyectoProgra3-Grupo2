package com.example.vamospues.ui.navigation

import android.os.SystemClock
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vamospues.Config
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.enums.EstadoParticipacion
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.screens.checkin.CheckInScreen
import com.example.vamospues.ui.screens.checkin.CheckInUiState
import java.time.LocalDate
import kotlinx.coroutines.delay

private const val DuracionQrMs = 102_000L
private const val DuracionSimulacionMs = 900L

private val estadosDemo = mapOf(
    "Organizador" to CheckInUiState.ORGANIZADOR,
    "Asistente" to CheckInUiState.ASISTENTE,
    "QR caducado" to CheckInUiState.QR_CADUCADO,
    "Validando GPS" to CheckInUiState.VALIDANDO_GPS,
    "Error GPS" to CheckInUiState.ERROR_GPS,
    "Fuera del área" to CheckInUiState.FUERA_DEL_AREA,
    "Asistencia confirmada" to CheckInUiState.ASISTENCIA_CONFIRMADA,
)

fun NavGraphBuilder.checkInGraph() {
    composable(
        route = Rutas.CHECKIN,
        arguments = listOf(navArgument(Rutas.ARG_LOBBY_ID) { type = NavType.StringType }),
    ) { entrada ->
        val lobbyId = entrada.arguments?.getString(Rutas.ARG_LOBBY_ID).orEmpty()
        val regreso = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        CheckInDestino(lobbyId, onVolver = { regreso?.onBackPressed() })
    }
}

@Composable
private fun CheckInDestino(lobbyId: String, onVolver: () -> Unit) {
    val lobby = remember(lobbyId) { FakeData.lobbies.firstOrNull { it.id == lobbyId } }
    val usuario = FakeData.usuarioActual
    val esOrganizador = lobby?.organizador?.id == usuario.id
    val participacion = lobby?.participaciones?.firstOrNull { it.participante.id == usuario.id }
    val fechaEvento = lobby?.fechaHora?.toLocalDate()
    val hoy = (if (Config.MODO_DEMO) fechaEvento else null) ?: LocalDate.now()
    val accesoPermitido = lobby != null && (
        Config.MODO_DEMO || (
            fechaEvento == hoy && (esOrganizador || participacion?.estado == EstadoParticipacion.CONFIRMADA)
        )
    )

    var state by rememberSaveable(lobbyId) {
        mutableStateOf(
            when {
                esOrganizador -> CheckInUiState.ORGANIZADOR
                participacion?.checkInRealizado == true -> CheckInUiState.ASISTENCIA_CONFIRMADA
                else -> CheckInUiState.ASISTENTE
            },
        )
    }
    var simulando by rememberSaveable(lobbyId) { mutableStateOf(false) }
    var vencimiento by rememberSaveable(lobbyId) {
        mutableLongStateOf(SystemClock.elapsedRealtime() + DuracionQrMs)
    }
    var segundosQr by remember(lobbyId) { mutableIntStateOf((DuracionQrMs / 1000).toInt()) }

    LaunchedEffect(state, vencimiento, accesoPermitido) {
        if (state == CheckInUiState.ORGANIZADOR && accesoPermitido) {
            do {
                val restante = (vencimiento - SystemClock.elapsedRealtime()).coerceAtLeast(0)
                segundosQr = ((restante + 999) / 1000).toInt()
                if (restante == 0L) {
                    state = CheckInUiState.QR_CADUCADO
                } else {
                    delay((restante - 1) % 1000 + 1)
                }
            } while (state == CheckInUiState.ORGANIZADOR)
        }
    }

    LaunchedEffect(simulando, state) {
        if (simulando) {
            delay(DuracionSimulacionMs)
            simulando = false
            state = CheckInUiState.ASISTENCIA_CONFIRMADA
        }
    }

    CheckInScreen(
        lobbyId = lobbyId,
        state = state,
        segundosQr = segundosQr,
        fechaDemo = hoy,
        accesoPermitido = accesoPermitido,
        onVolver = onVolver,
        onEscanear = { simulando = true },
        onValidarGps = {
            if (!simulando) {
                state = CheckInUiState.VALIDANDO_GPS
                simulando = true
            }
        },
        onRenovarCodigo = {
            vencimiento = SystemClock.elapsedRealtime() + DuracionQrMs
            state = CheckInUiState.ORGANIZADOR
        },
        selectorDemo = if (Config.MODO_DEMO) {
            {
                DemoEstadoSelector(
                    estados = estadosDemo.keys.toList(),
                    estadoActual = estadosDemo.entries.first { it.value == state }.key,
                    onEstadoSelected = { etiqueta ->
                        simulando = false
                        val seleccionado = estadosDemo.getValue(etiqueta)
                        if (seleccionado == CheckInUiState.ORGANIZADOR) {
                            vencimiento = SystemClock.elapsedRealtime() + DuracionQrMs
                        }
                        state = seleccionado
                    },
                )
            }
        } else null,
    )
}
