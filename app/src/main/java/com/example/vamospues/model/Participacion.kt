package com.example.vamospues.model

import com.example.vamospues.model.enums.EstadoParticipacion
import java.time.LocalDateTime

class Participacion internal constructor(
    val participante: Usuario,
    val fechaUnion: LocalDateTime = LocalDateTime.now(),
    estado: EstadoParticipacion = EstadoParticipacion.PENDIENTE,
    checkInRealizado: Boolean = false,
    private val lobby: Lobby,
) {
    var estado: EstadoParticipacion = estado
        private set

    var checkInRealizado: Boolean = checkInRealizado
        private set

    fun confirmar() {
        if (estado == EstadoParticipacion.CONFIRMADA) return
        lobby.confirmarParticipacion {
            estado = EstadoParticipacion.CONFIRMADA
        }
    }

    fun marcarCheckIn() {
        checkInRealizado = true
    }
}
