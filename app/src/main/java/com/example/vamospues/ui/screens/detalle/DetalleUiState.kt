package com.example.vamospues.ui.screens.detalle

import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Usuario
import com.example.vamospues.model.enums.EstadoParticipacion
import com.example.vamospues.model.enums.TipoAcceso
import com.example.vamospues.model.estado.Activo
import com.example.vamospues.model.estado.Bloqueado
import com.example.vamospues.model.estado.EnProceso
import com.example.vamospues.model.estado.Finalizado
import com.example.vamospues.model.estado.Lleno
import com.example.vamospues.model.estado.PendienteModeracion
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Acción contextual del botón inferior del detalle (una por variante del Figma). */
enum class AccionDetalle(val etiqueta: String) {
    UNIRME("Unirme"),
    SOLICITAR("Solicitar"),
    PENDIENTE("Pendiente"),
    CUPO_LLENO("Cupo lleno"),
    CHECK_IN("Check-in"),
    EN_PROCESO("En proceso"),
    FINALIZADO("Finalizado"),
    BLOQUEADO("Bloqueado"),
    EN_REVISION("En revisión"),
}

/** Datos de presentación de solo lectura derivados de un [Lobby]. */
data class ParticipanteUi(val nombre: String, val estadoTexto: String)

data class DetalleLobbyUi(
    val id: String,
    val titulo: String,
    val resumen: String,
    val esPrivado: Boolean,
    val requiereAprobacion: Boolean,
    val organizador: String,
    val karmaOrganizador: Int,
    val nombreLugar: String,
    val direccion: String,
    val referencia: String,
    val confirmados: Int,
    val cupoMaximo: Int,
    val participantes: List<ParticipanteUi>,
    val nombreUsuarioActual: String,
) {
    val cuposDisponibles: Int get() = (cupoMaximo - confirmados).coerceAtLeast(0)
}

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Error(val noEncontrado: Boolean = false) : DetalleUiState
    data class Contenido(val lobby: DetalleLobbyUi, val accion: AccionDetalle) : DetalleUiState
}

private const val TextoConfirmado = "Confirmado"
private const val TextoPendiente = "Pendiente"

fun textoEstadoParticipacion(estado: EstadoParticipacion): String = when (estado) {
    EstadoParticipacion.CONFIRMADA -> TextoConfirmado
    EstadoParticipacion.PENDIENTE -> TextoPendiente
    EstadoParticipacion.RECHAZADA -> "Rechazado"
    EstadoParticipacion.CANCELADA -> "Cancelado"
}

/** Construye la vista del lobby sin modificar el dominio. */
fun detalleLobbyUi(lobby: Lobby, usuario: Usuario, hoy: LocalDate): DetalleLobbyUi {
    val nivel = lobby.nivel.name.lowercase().replaceFirstChar { it.uppercase() }
    return DetalleLobbyUi(
        id = lobby.id,
        titulo = lobby.titulo,
        resumen = "${textoFecha(lobby.fechaHora, hoy)} · ${lobby.categoria.nombre} · $nivel",
        esPrivado = !lobby.esPublico(),
        requiereAprobacion = lobby.tipoAcceso == TipoAcceso.CON_APROBACION,
        organizador = lobby.organizador.nombre,
        karmaOrganizador = lobby.organizador.reputacionKarma,
        nombreLugar = lobby.ubicacion.nombreLugar,
        direccion = lobby.ubicacion.direccion,
        referencia = lobby.ubicacion.referencia,
        confirmados = lobby.cupoMaximo - lobby.cuposDisponibles(),
        cupoMaximo = lobby.cupoMaximo,
        participantes = lobby.participaciones.map {
            ParticipanteUi(it.participante.nombre, textoEstadoParticipacion(it.estado))
        },
        nombreUsuarioActual = usuario.nombre,
    )
}

/**
 * Variante del botón principal según el estado del lobby y la relación del usuario con él.
 * La validación de fecha y ubicación del check-in la hace la pantalla de Check-in.
 */
fun accionPorDefecto(lobby: Lobby, usuario: Usuario): AccionDetalle {
    val participacion = lobby.participaciones.firstOrNull { it.participante.id == usuario.id }
    val confirmado = lobby.organizador.id == usuario.id ||
        participacion?.estado == EstadoParticipacion.CONFIRMADA
    return when (lobby.estadoActual) {
        is PendienteModeracion -> AccionDetalle.EN_REVISION
        is Bloqueado -> AccionDetalle.BLOQUEADO
        is Finalizado -> AccionDetalle.FINALIZADO
        is EnProceso -> AccionDetalle.EN_PROCESO
        is Lleno -> if (confirmado) AccionDetalle.CHECK_IN else AccionDetalle.CUPO_LLENO
        is Activo -> when {
            participacion?.estado == EstadoParticipacion.PENDIENTE -> AccionDetalle.PENDIENTE
            confirmado -> AccionDetalle.CHECK_IN
            lobby.cuposDisponibles() == 0 -> AccionDetalle.CUPO_LLENO
            lobby.tipoAcceso == TipoAcceso.CON_APROBACION || !lobby.esPublico() -> AccionDetalle.SOLICITAR
            else -> AccionDetalle.UNIRME
        }
    }
}

/** Participantes mostrados como extracto: tres filas, o la solicitud propia en la variante Pendiente. */
fun participantesVisibles(lobby: DetalleLobbyUi, accion: AccionDetalle): List<ParticipanteUi> {
    val confirmados = lobby.participantes.filter { it.estadoTexto == TextoConfirmado }
    return if (accion == AccionDetalle.PENDIENTE) {
        confirmados.take(2) + ParticipanteUi(lobby.nombreUsuarioActual, TextoPendiente)
    } else {
        lobby.participantes.take(3)
    }
}

private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")
private val formatoFecha = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", Locale.forLanguageTag("es"))

fun textoFecha(fechaHora: LocalDateTime, hoy: LocalDate): String = when (fechaHora.toLocalDate()) {
    hoy -> "Hoy, ${fechaHora.format(formatoHora)}"
    hoy.plusDays(1) -> "Mañana, ${fechaHora.format(formatoHora)}"
    else -> fechaHora.format(formatoFecha).replaceFirstChar { it.uppercase() }
}
