package com.example.vamospues.ui.navigation

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
import androidx.navigation.compose.composable
import com.example.vamospues.Config
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.enums.EstadoParticipacion
import com.example.vamospues.model.estado.Finalizado
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.screens.chat.ChatScreen
import com.example.vamospues.ui.screens.chat.ChatUiState
import com.example.vamospues.ui.screens.chat.EncuestaChatUi
import com.example.vamospues.ui.screens.chat.MensajeChatUi
import com.example.vamospues.ui.screens.chat.encuestaChatDemo
import com.example.vamospues.ui.screens.chat.mensajesChatDemo
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MinimoConfirmados = 6
private const val ConfirmadosDemoBloqueado = 4
private const val DemoraReintentoMs = 850L

fun NavGraphBuilder.chatGraph() {
    composable(Rutas.CHAT) { ChatDestino() }
}

@Composable
private fun ChatDestino() {
    val lobby = remember { FakeData.lobbies.firstOrNull { it.id == "lobby-1" } }
    val confirmados = lobby?.participaciones?.count { it.estado == EstadoParticipacion.CONFIRMADA } ?: 0
    val conversacionDemo = remember { ChatUiState.Conversacion(mensajesChatDemo, listOf(encuestaChatDemo)) }
    val soloLecturaDemo = remember { ChatUiState.SoloLectura(mensajesChatDemo, listOf(encuestaChatDemo)) }
    val estadoPorDefecto = when {
        lobby == null -> ChatUiState.Error
        lobby.estadoActual is Finalizado -> soloLecturaDemo
        confirmados < MinimoConfirmados -> ChatUiState.Bloqueado
        else -> conversacionDemo
    }

    var state by remember { mutableStateOf(estadoPorDefecto) }
    var borrador by rememberSaveable { mutableStateOf("") }
    var reintentando by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val estadosDemo = remember {
        linkedMapOf(
            "Conversación" to conversacionDemo,
            "Sin mensajes" to ChatUiState.SinMensajes,
            "Bloqueado" to ChatUiState.Bloqueado,
            "Solo lectura" to soloLecturaDemo,
            "Cargando" to ChatUiState.Cargando,
            "Error" to ChatUiState.Error,
        )
    }

    LaunchedEffect(reintentando) {
        if (reintentando) {
            delay(DemoraReintentoMs)
            state = estadoPorDefecto
            reintentando = false
        }
    }

    ChatScreen(
        state = state,
        tituloLobby = lobby?.titulo.orEmpty(),
        confirmados = if (state == ChatUiState.Bloqueado && Config.MODO_DEMO) ConfirmadosDemoBloqueado else confirmados,
        minimoConfirmados = MinimoConfirmados,
        borrador = borrador,
        onBorradorChange = { borrador = it },
        onEnviar = {
            val texto = borrador.trim()
            val conversacion = when (val actual = state) {
                is ChatUiState.Conversacion -> actual
                ChatUiState.SinMensajes -> ChatUiState.Conversacion(emptyList())
                else -> null
            }
            if (texto.isNotEmpty() && conversacion != null) {
                val mensaje = MensajeChatUi(UUID.randomUUID().toString(), "Tú", texto, LocalTime.now(), true)
                state = conversacion.copy(mensajes = conversacion.mensajes + mensaje)
                borrador = ""
            }
        },
        onVotar = { encuestaId, opcionId ->
            val conversacion = state as? ChatUiState.Conversacion
            if (conversacion != null) {
                state = conversacion.copy(encuestas = conversacion.encuestas.map { encuesta ->
                    if (encuesta.id == encuestaId) cambiarVoto(encuesta, opcionId) else encuesta
                })
            }
        },
        onCompartirUbicacion = {
            scope.launch { snackbar.showSnackbar("Ubicación compartida (demo).") }
        },
        onReintentar = {
            state = ChatUiState.Cargando
            reintentando = true
        },
        snackbarHostState = snackbar,
        selectorDemo = if (Config.MODO_DEMO) {
            {
                DemoEstadoSelector(
                    estados = estadosDemo.keys.toList(),
                    estadoActual = estadosDemo.entries.first { it.value::class == state::class }.key,
                    onEstadoSelected = { etiqueta ->
                        val seleccionado = estadosDemo.getValue(etiqueta)
                        if (seleccionado::class != state::class) {
                            reintentando = false
                            state = seleccionado
                        }
                    },
                )
            }
        } else null,
    )
}

private fun cambiarVoto(encuesta: EncuestaChatUi, opcionId: String): EncuestaChatUi {
    val anterior = encuesta.opcionSeleccionadaId
    if (anterior == opcionId || encuesta.opciones.none { it.id == opcionId }) return encuesta
    return encuesta.copy(
        opcionSeleccionadaId = opcionId,
        opciones = encuesta.opciones.map { opcion ->
            when (opcion.id) {
                opcionId -> opcion.copy(votos = opcion.votos + 1)
                anterior -> opcion.copy(votos = (opcion.votos - 1).coerceAtLeast(0))
                else -> opcion
            }
        },
    )
}
