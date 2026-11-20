package com.example.vamospues.data

import com.example.vamospues.model.Administrador
import com.example.vamospues.model.Categoria
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Reporte
import com.example.vamospues.model.Usuario
import com.example.vamospues.model.enums.EstadoAnalisis
import com.example.vamospues.model.enums.EstadoCuenta
import com.example.vamospues.model.enums.EstadoReporte
import com.example.vamospues.model.enums.NivelHabilidad
import com.example.vamospues.model.enums.TipoAcceso
import com.example.vamospues.model.enums.Visibilidad
import com.example.vamospues.model.estado.Activo
import com.example.vamospues.model.moderacion.FiltroIA
import com.example.vamospues.model.moderacion.FiltroPalabrasProhibidas
import com.example.vamospues.model.moderacion.FiltroRevisionHumana
import com.example.vamospues.model.moderacion.ResultadoAnalisis
import com.example.vamospues.model.moderacion.ServicioModeracion
import java.time.LocalDateTime

object FakeData {
    val administrador = Administrador("admin", "Elena Paz", "elena@example.com", nivelAcceso = 1)
    val lucia = Usuario("lucia", "Lucía Ramos", "lucia@example.com", reputacionKarma = 128)
    val diego = Usuario("diego", "Diego Soto", "diego@example.com")
    val ana = Usuario("ana", "Ana Ríos", "ana@example.com")
    val usuarioActual = Usuario("nicolas", "Nicolás Herrera", "nicolas@example.com")
    val usuarios = listOf(
        lucia,
        diego,
        ana,
        Usuario("mateo", "Mateo Cruz", "mateo@example.com"),
        Usuario("sofia", "Sofía Vargas", "sofia@example.com"),
        Usuario("valeria", "Valeria Torres", "valeria@example.com"),
        Usuario("gabriel", "Gabriel Flores", "gabriel@example.com"),
        Usuario("camila", "Camila Mendoza", "camila@example.com"),
        Usuario("martina", "Martina Salazar", "martina@example.com"),
        usuarioActual,
    )

    val usuarioSuspendido = Usuario(
        "bruno", "Bruno Molina", "bruno@example.com", estado = EstadoCuenta.SUSPENDIDA,
    )
    val usuarioBaneado = Usuario(
        "lara", "Lara Ortiz", "lara@example.com", estado = EstadoCuenta.BANEADA,
    )

    val futbol = Categoria("futbol", "Fútbol", requiereVerificacionEdad = false)
    val voley = Categoria("voley", "Vóley", requiereVerificacionEdad = false)
    val cine = Categoria("cine", "Cine", requiereVerificacionEdad = false)
    val juegosDeMesa = Categoria("juegos", "Juegos de mesa", requiereVerificacionEdad = false)
    val categorias = listOf(futbol, voley, cine, juegosDeMesa)

    private val filtroPalabras = FiltroPalabrasProhibidas("palabras", "Palabras prohibidas").apply {
        listOf("insulto", "amenaza", "estafa").forEach(::agregarPalabra)
    }
    private val filtroIA = FiltroIA(
        "ia", "Análisis simulado", apiEndpoint = "https://example.invalid/analisis", umbralSensibilidad = 0.7f,
    )
    private val filtroHumano = FiltroRevisionHumana(
        "revision-humana", "Revisión humana", revisanLosAdmins = listOf(administrador),
    )
    val servicioModeracion = ServicioModeracion(filtroPalabras, filtroIA, filtroHumano)
    val filtros get() = servicioModeracion.filtros

    val lobbies: List<Lobby> = listOf(
        lucia.crearLobby(
            id = "lobby-1",
            titulo = "Fútbol al caer la tarde",
            descripcion = "Partido abierto en Cancha Los Pinos.",
            fechaHora = LocalDateTime.of(2026, 12, 4, 19, 0),
            cupoMaximo = 10,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = futbol,
            latitud = -16.50,
            longitud = -68.13,
            nombreLugar = "Cancha Los Pinos",
            direccion = "Av. Los Pinos",
            referencia = "Entrada principal",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0))
            cambiarEstado(Activo())
            usuarios.filterNot { it === usuarioActual }.take(8).forEach { it.unirseALobby(this) }
        },
        diego.crearLobby(
            id = "lobby-2",
            titulo = "Partido entre amigos",
            descripcion = "Partido de nivel avanzado.",
            fechaHora = LocalDateTime.of(2026, 12, 5, 18, 0),
            cupoMaximo = 10,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.AVANZADO,
            categoria = futbol,
            latitud = -16.48,
            longitud = -68.14,
            nombreLugar = "Polideportivo Norte",
            direccion = "Av. Norte",
            referencia = "Cancha techada",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0))
            cambiarEstado(Activo())
            usuarios.filterNot { it === usuarioActual }.take(6).forEach { it.unirseALobby(this) }
        },
        ana.crearLobby(
            id = "lobby-3",
            titulo = "Noche de juegos de mesa",
            descripcion = "Una partida con invitación.",
            fechaHora = LocalDateTime.of(2026, 12, 6, 17, 0),
            cupoMaximo = 6,
            tipoAcceso = TipoAcceso.CON_APROBACION,
            visibilidad = Visibilidad.PRIVADO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = juegosDeMesa,
            latitud = -16.51,
            longitud = -68.12,
            nombreLugar = "Sala de juegos",
            direccion = "Calle Central",
            referencia = "Segundo piso",
            codigoInvitacion = "VP-7K2M",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.PENDIENTE_REVISION, nivelRiesgo = 0))
            registrarAnalisis(
                ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0, revisadoPor = administrador),
            )
            cambiarEstado(Activo())
            usuarios.take(3).forEach { it.unirseALobby(this).confirmar() }
            usuarios[3].unirseALobby(this)
        },
        lucia.crearLobby(
            id = "lobby-4",
            titulo = "Copa relámpago de fútbol",
            descripcion = "Partido con todos los cupos ocupados.",
            fechaHora = LocalDateTime.of(2026, 12, 8, 16, 0),
            cupoMaximo = 10,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = futbol,
            latitud = -16.50,
            longitud = -68.12,
            nombreLugar = "Cancha Central",
            direccion = "Av. Central",
            referencia = "Cancha 2",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0))
            cambiarEstado(Activo())
            usuarios.forEach { it.unirseALobby(this) }
        },
        diego.crearLobby(
            id = "lobby-5",
            titulo = "Vóley de media tarde",
            descripcion = "El partido ya empezó.",
            fechaHora = LocalDateTime.of(2026, 12, 9, 16, 0),
            cupoMaximo = 8,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = voley,
            latitud = -16.50,
            longitud = -68.12,
            nombreLugar = "Complejo Deportivo Sur",
            direccion = "Av. Sur",
            referencia = "Cancha de arena",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0))
            cambiarEstado(Activo())
            usuarios.take(4).forEach { it.unirseALobby(this).marcarCheckIn() }
            estadoActual.iniciar(this)
        },
        ana.crearLobby(
            id = "lobby-6",
            titulo = "Cine de domingo",
            descripcion = "Función de cine comunitaria.",
            fechaHora = LocalDateTime.of(2026, 12, 10, 16, 0),
            cupoMaximo = 8,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = cine,
            latitud = -16.50,
            longitud = -68.12,
            nombreLugar = "Cine del Barrio",
            direccion = "Calle del Teatro",
            referencia = "Sala 1",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0))
            cambiarEstado(Activo())
            usuarios.take(5).forEach { it.unirseALobby(this).marcarCheckIn() }
            estadoActual.iniciar(this)
            estadoActual.finalizar(this)
        },
        lucia.crearLobby(
            id = "lobby-7",
            titulo = "Vóley en el parque",
            descripcion = "Convocatoria retirada en moderación.",
            fechaHora = LocalDateTime.of(2026, 12, 7, 16, 0),
            cupoMaximo = 12,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = voley,
            latitud = -16.49,
            longitud = -68.11,
            nombreLugar = "Parque Vecinal",
            direccion = "Av. del Parque",
            referencia = "Cancha exterior",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.APROBADO, nivelRiesgo = 0))
            cambiarEstado(Activo())
            diego.unirseALobby(this)
            registrarAnalisis(
                ResultadoAnalisis(
                    EstadoAnalisis.RECHAZADO,
                    nivelRiesgo = 1,
                    motivoRechazo = "Convocatoria de prueba retirada.",
                    revisadoPor = administrador,
                ),
            )
            estadoActual.bloquear(this)
        },
        usuarioActual.crearLobby(
            id = "lobby-8",
            titulo = "Vóley de fin de semana",
            descripcion = "Convocatoria en espera de revisión.",
            fechaHora = LocalDateTime.of(2026, 12, 12, 15, 0),
            cupoMaximo = 8,
            tipoAcceso = TipoAcceso.AUTOMATICO,
            visibilidad = Visibilidad.PUBLICO,
            nivel = NivelHabilidad.PRINCIPIANTE,
            categoria = voley,
            latitud = -16.50,
            longitud = -68.12,
            nombreLugar = "Cancha del Barrio",
            direccion = "Calle del Parque",
            referencia = "Junto al acceso principal",
        ).apply {
            registrarAnalisis(ResultadoAnalisis(EstadoAnalisis.PENDIENTE_REVISION, nivelRiesgo = 2))
            filtroHumano.encolar(this)
        },
    )

    val reportes = listOf(
        Reporte("reporte-1", "La descripción del evento requiere revisión.", lobbies[7]),
        Reporte(
            "reporte-2", "Conducta reiterada en actividades anteriores.", usuarioSuspendido,
            estado = EstadoReporte.RESUELTO, revisadoPor = administrador,
        ),
        Reporte(
            "reporte-3", "Reporte duplicado sobre el horario.", lobbies[0],
            estado = EstadoReporte.DESCARTADO, revisadoPor = administrador,
        ),
    )
}
