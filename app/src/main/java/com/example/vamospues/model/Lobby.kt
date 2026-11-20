package com.example.vamospues.model

import com.example.vamospues.model.enums.EstadoParticipacion
import com.example.vamospues.model.enums.NivelHabilidad
import com.example.vamospues.model.enums.TipoAcceso
import com.example.vamospues.model.enums.Visibilidad
import com.example.vamospues.model.estado.Activo
import com.example.vamospues.model.estado.Bloqueado
import com.example.vamospues.model.estado.EnProceso
import com.example.vamospues.model.estado.EstadoLobby
import com.example.vamospues.model.estado.Finalizado
import com.example.vamospues.model.estado.Lleno
import com.example.vamospues.model.estado.PendienteModeracion
import com.example.vamospues.model.moderacion.ResultadoAnalisis
import java.time.LocalDateTime

class Lobby(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaHora: LocalDateTime,
    val cupoMaximo: Int,
    val tipoAcceso: TipoAcceso,
    val visibilidad: Visibilidad,
    val codigoInvitacion: String,
    val categoria: Categoria,
    latitud: Double,
    longitud: Double,
    nombreLugar: String,
    direccion: String,
    referencia: String,
    val nivel: NivelHabilidad,
    val organizador: Usuario,
) : Reportable {
    val ubicacion = Ubicacion(latitud, longitud, nombreLugar, direccion, referencia)

    private var estado: EstadoLobby = PendienteModeracion()
    val estadoActual: EstadoLobby get() = estado

    private val participacionesRegistradas = mutableListOf<Participacion>()
    val participaciones: List<Participacion> get() = participacionesRegistradas.toList()

    private val analisisRegistrados = mutableListOf<ResultadoAnalisis>()
    val historialAnalisis: List<ResultadoAnalisis> get() = analisisRegistrados.toList()

    override fun obtenerId(): String = id

    fun agregarParticipante(usuario: Usuario): Participacion =
        estadoActual.agregarParticipante(this, usuario)

    internal fun registrarParticipante(usuario: Usuario): Participacion {
        check(estadoActual is Activo && cuposDisponibles() > 0) {
            "El lobby debe estar activo y tener cupos disponibles."
        }
        // Fase 2: validar elegibilidad y una sola inscripción por usuario.
        val participacion = Participacion(usuario, lobby = this)
        participacionesRegistradas.add(participacion)
        if (tipoAcceso == TipoAcceso.AUTOMATICO) participacion.confirmar()
        usuario.registrarParticipacion(participacion)
        return participacion
    }

    internal fun confirmarParticipacion(confirmar: () -> Unit) {
        check(cuposDisponibles() > 0) { "No quedan cupos para confirmar la participación." }
        confirmar()
        if (estado is Activo && cuposDisponibles() == 0) cambiarEstado(Lleno())
    }

    fun cambiarEstado(nuevo: EstadoLobby) {
        val permitido = when (estado) {
            is PendienteModeracion -> nuevo is Activo || nuevo is Bloqueado
            is Activo -> nuevo is Lleno || nuevo is EnProceso || nuevo is Bloqueado
            is Lleno -> nuevo is Activo || nuevo is EnProceso
            is EnProceso -> nuevo is Finalizado
            is Finalizado, is Bloqueado -> false
        }
        check(permitido) {
            "Transición de ${estadoActual.javaClass.simpleName} a ${nuevo.javaClass.simpleName} no permitida."
        }
        check(nuevo !is Lleno || cantidadConfirmadas() == cupoMaximo) {
            "El lobby solo puede pasar a Lleno cuando todos los cupos están confirmados."
        }
        estado = nuevo
    }

    fun cuposDisponibles(): Int = (cupoMaximo - cantidadConfirmadas()).coerceAtLeast(0)

    private fun cantidadConfirmadas(): Int =
        participacionesRegistradas.count { it.estado == EstadoParticipacion.CONFIRMADA }

    fun esPublico(): Boolean = visibilidad == Visibilidad.PUBLICO

    internal fun textoParaModeracion(): String = "$titulo\n$descripcion"

    fun registrarAnalisis(resultado: ResultadoAnalisis) {
        analisisRegistrados.add(ResultadoAnalisis(resultado))
    }
}
