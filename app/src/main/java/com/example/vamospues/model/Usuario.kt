package com.example.vamospues.model

import com.example.vamospues.model.enums.EstadoCuenta
import com.example.vamospues.model.enums.NivelHabilidad
import com.example.vamospues.model.enums.TipoAcceso
import com.example.vamospues.model.enums.Visibilidad
import java.time.LocalDateTime
import java.util.UUID

open class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val reputacionKarma: Int = 0,
    estado: EstadoCuenta = EstadoCuenta.ACTIVA,
) : Reportable {
    var estado: EstadoCuenta = estado
        private set

    private val lobbiesCreados = mutableListOf<Lobby>()
    val crea: List<Lobby> get() = lobbiesCreados.toList()

    private val participacionesRegistradas = mutableListOf<Participacion>()
    val participaciones: List<Participacion> get() = participacionesRegistradas.toList()

    private val reportesEmitidos = mutableListOf<Reporte>()
    val emite: List<Reporte> get() = reportesEmitidos.toList()

    override fun obtenerId(): String = id

    fun crearLobby(
        titulo: String,
        descripcion: String,
        fechaHora: LocalDateTime,
        cupoMaximo: Int,
        tipoAcceso: TipoAcceso,
        visibilidad: Visibilidad,
        categoria: Categoria,
        latitud: Double,
        longitud: Double,
        nombreLugar: String,
        direccion: String,
        referencia: String,
        nivel: NivelHabilidad,
        codigoInvitacion: String = "",
        id: String = UUID.randomUUID().toString(),
    ): Lobby {
        // Fase 2: validar los datos y enviar el lobby a moderación.
        return Lobby(
            id, titulo, descripcion, fechaHora,
            cupoMaximo, tipoAcceso, visibilidad, codigoInvitacion, categoria,
            latitud, longitud, nombreLugar, direccion, referencia,
            nivel = nivel,
            organizador = this,
        ).also(lobbiesCreados::add)
    }

    fun unirseALobby(lobby: Lobby): Participacion = lobby.agregarParticipante(this)

    internal fun registrarParticipacion(participacion: Participacion) {
        participacionesRegistradas.add(participacion)
    }

    fun reportarLobby(lobby: Lobby, motivo: String): Reporte {
        // Fase 2: validar y enviar el reporte a moderación.
        return Reporte(UUID.randomUUID().toString(), motivo, lobby).also(reportesEmitidos::add)
    }

    fun realizarCheckIn(lobby: Lobby) {
        // Fase 2: validar el horario y la ubicación del check-in.
        lobby.participaciones.firstOrNull { it.participante === this }?.marcarCheckIn()
    }
}
