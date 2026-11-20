package com.example.vamospues.ui.screens.chat

import java.time.LocalTime

sealed interface ChatUiState {
    data class Conversacion(
        val mensajes: List<MensajeChatUi>,
        val encuestas: List<EncuestaChatUi> = emptyList(),
    ) : ChatUiState

    data object SinMensajes : ChatUiState

    data object Bloqueado : ChatUiState

    data class SoloLectura(
        val mensajes: List<MensajeChatUi>,
        val encuestas: List<EncuestaChatUi> = emptyList(),
    ) : ChatUiState

    data object Cargando : ChatUiState

    data object Error : ChatUiState
}

data class MensajeChatUi(
    val id: String,
    val autor: String,
    val texto: String,
    val hora: LocalTime,
    val esPropio: Boolean,
)

data class EncuestaChatUi(
    val id: String,
    val pregunta: String,
    val opciones: List<OpcionEncuestaUi>,
    val opcionSeleccionadaId: String? = null,
)

data class OpcionEncuestaUi(
    val id: String,
    val texto: String,
    val votos: Int = 0,
)
