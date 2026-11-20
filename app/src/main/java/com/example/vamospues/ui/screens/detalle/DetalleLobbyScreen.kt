package com.example.vamospues.ui.screens.detalle

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.components.EstadoChip
import com.example.vamospues.ui.components.ParticipanteItem
import com.example.vamospues.ui.components.UbicacionBlock
import com.example.vamospues.ui.theme.MyApplicationTheme

private const val TituloPantalla = "Detalle del lobby"

/** Texto del chip, nota bajo participantes y botón para cada variante del Figma. */
private data class PresentacionAccion(
    val chip: String,
    val nota: String,
    val boton: String,
    val habilitado: Boolean,
    val alerta: Boolean = false,
)

private fun presentacion(lobby: DetalleLobbyUi, accion: AccionDetalle): PresentacionAccion {
    val visibles = participantesVisibles(lobby, accion).count { it.estadoTexto == "Confirmado" }
    val restantes = lobby.confirmados - visibles
    val confirmadosExtra = if (restantes > 0) {
        "+$restantes participantes confirmados"
    } else {
        "${lobby.confirmados} participantes confirmados"
    }
    val lugares = when (lobby.cuposDisponibles) {
        1 -> "Queda 1 lugar"
        else -> "Quedan ${lobby.cuposDisponibles} lugares"
    }
    val chipBuscando = "Buscando gente"
    val chipLleno = "Cupo lleno"
    return when (accion) {
        AccionDetalle.UNIRME -> PresentacionAccion(chipBuscando, "$lugares · Acceso directo", "Unirme", true)
        AccionDetalle.SOLICITAR -> PresentacionAccion(
            chip = chipBuscando,
            nota = if (lobby.esPrivado) "Invitación válida · Requiere aprobación" else "$lugares · Requiere aprobación",
            boton = "Solicitar unirme",
            habilitado = true,
        )
        AccionDetalle.PENDIENTE -> PresentacionAccion(
            chipBuscando, "Tu solicitud está en espera; no ocupa cupo.", "Pendiente de aprobación", false,
        )
        AccionDetalle.CUPO_LLENO -> PresentacionAccion(chipLleno, confirmadosExtra, "Cupo lleno", false)
        AccionDetalle.CHECK_IN -> PresentacionAccion(
            chip = if (lobby.cuposDisponibles == 0) chipLleno else chipBuscando,
            nota = "El check-in está disponible hoy.",
            boton = "Check-in",
            habilitado = true,
        )
        AccionDetalle.EN_PROCESO -> PresentacionAccion("En proceso", confirmadosExtra, "Evento en proceso", false)
        AccionDetalle.FINALIZADO -> PresentacionAccion(
            "Finalizado", "El chat está en modo solo lectura.", "Evento finalizado", false,
        )
        AccionDetalle.BLOQUEADO -> PresentacionAccion(
            "Bloqueado", "Bloqueado por incumplir las normas de seguridad.", "Lobby bloqueado", false, alerta = true,
        )
        AccionDetalle.EN_REVISION -> PresentacionAccion(
            "En revisión", "El lobby aún no es visible: está en revisión de moderación.", "En revisión", false,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLobbyScreen(
    state: DetalleUiState,
    menuAbierto: Boolean,
    onMenuAbiertoChange: (Boolean) -> Unit,
    onVolver: () -> Unit,
    onReportar: () -> Unit,
    onComoLlegar: () -> Unit,
    onAccion: (AccionDetalle) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(TituloPantalla) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (state is DetalleUiState.Contenido) {
                        Box {
                            IconButton(onClick = { onMenuAbiertoChange(true) }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(
                                expanded = menuAbierto,
                                onDismissRequest = { onMenuAbiertoChange(false) },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Reportar", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        onMenuAbiertoChange(false)
                                        onReportar()
                                    },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            if (state is DetalleUiState.Contenido) {
                BotonAccion(state.lobby, state.accion, onAccion)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            selectorDemo?.invoke()
            when (state) {
                DetalleUiState.Cargando -> DetalleCargando()
                is DetalleUiState.Error -> DetalleError(state.noEncontrado, onReintentar, onVolver)
                is DetalleUiState.Contenido -> DetalleContenido(state.lobby, state.accion, onComoLlegar)
            }
        }
    }
}

@Composable
private fun DetalleContenido(lobby: DetalleLobbyUi, accion: AccionDetalle, onComoLlegar: () -> Unit) {
    val presentacion = presentacion(lobby, accion)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column {
                Text(
                    text = lobby.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = lobby.resumen,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics(mergeDescendants = true) {
                        stateDescription = buildString {
                            append("Estado: ${presentacion.chip}")
                            if (lobby.esPrivado) append(". Lobby privado")
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EstadoChip(
                    text = presentacion.chip,
                    contentColor = if (presentacion.alerta) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                if (lobby.esPrivado) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Privado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
        item { OrganizadorFila(lobby.organizador, lobby.karmaOrganizador) }
        item {
            UbicacionBlock(
                nombreLugar = lobby.nombreLugar,
                direccion = lobby.direccion,
                referencia = lobby.referencia,
                onComoLlegarClick = onComoLlegar,
            )
        }
        item {
            Text(
                text = "Participantes · ${lobby.confirmados} de ${lobby.cupoMaximo}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .semantics {
                        heading()
                        contentDescription =
                            "Participantes: ${lobby.confirmados} de ${lobby.cupoMaximo} cupos ocupados"
                    },
            )
        }
        items(participantesVisibles(lobby, accion)) { participante ->
            ParticipanteItem(nombre = participante.nombre, estadoTexto = participante.estadoTexto)
        }
        item {
            Text(
                text = presentacion.nota,
                style = MaterialTheme.typography.labelSmall,
                color = if (presentacion.alerta) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun OrganizadorFila(nombre: String, karma: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "Avatar de $nombre",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Organizador/a · Karma $karma",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun BotonAccion(lobby: DetalleLobbyUi, accion: AccionDetalle, onAccion: (AccionDetalle) -> Unit) {
    val presentacion = presentacion(lobby, accion)
    Surface(color = MaterialTheme.colorScheme.surface) {
        Button(
            onClick = { onAccion(accion) },
            enabled = presentacion.habilitado,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 48.dp)
                .semantics {
                    if (!presentacion.habilitado) stateDescription = "No disponible"
                },
        ) {
            if (accion == AccionDetalle.CHECK_IN) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
            }
            Text(presentacion.boton, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun DetalleCargando() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Cargando el lobby" },
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = "Cargando el lobby…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        repeat(3) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
            ) {}
        }
    }
}

@Composable
private fun DetalleError(noEncontrado: Boolean, onReintentar: () -> Unit, onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No pudimos abrir el lobby",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (noEncontrado) {
                "Este lobby no existe o ya no está disponible."
            } else {
                "Revisa tu conexión e inténtalo nuevamente."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = if (noEncontrado) onVolver else onReintentar,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Icon(
                imageVector = if (noEncontrado) Icons.AutoMirrored.Rounded.ArrowBack else Icons.Rounded.Refresh,
                contentDescription = null,
            )
            Spacer(Modifier.weight(1f))
            Text(if (noEncontrado) "Volver" else "Reintentar")
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
    }
}

// ---------- Previews ----------

private val lobbyMuestra = DetalleLobbyUi(
    id = "lobby-1",
    titulo = "Fútbol al caer la tarde",
    resumen = "Hoy, 19:00 · Fútbol · Principiante",
    esPrivado = false,
    requiereAprobacion = false,
    organizador = "Lucía Ramos",
    karmaOrganizador = 128,
    nombreLugar = "Cancha Los Pinos",
    direccion = "Av. Los Pinos",
    referencia = "Entrada principal",
    confirmados = 8,
    cupoMaximo = 10,
    participantes = listOf("Lucía Ramos", "Diego Soto", "Ana Ríos", "Mateo Cruz", "Sofía Vargas")
        .map { ParticipanteUi(it, "Confirmado") },
    nombreUsuarioActual = "Nicolás Herrera",
)

private val lobbyLleno = lobbyMuestra.copy(confirmados = 10)

@Composable
private fun PreviewDetalle(state: DetalleUiState, menuAbierto: Boolean = false) {
    MyApplicationTheme {
        DetalleLobbyScreen(
            state = state,
            menuAbierto = menuAbierto,
            onMenuAbiertoChange = {},
            onVolver = {},
            onReportar = {},
            onComoLlegar = {},
            onAccion = {},
            onReintentar = {},
            selectorDemo = {
                DemoEstadoSelector(
                    estados = AccionDetalle.entries.map { it.etiqueta },
                    estadoActual = (state as? DetalleUiState.Contenido)?.accion?.etiqueta.orEmpty(),
                    onEstadoSelected = {},
                )
            },
        )
    }
}

@Preview(name = "Unirme", showBackground = true, heightDp = 800)
@Preview(name = "Unirme · Oscuro", showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetalleUnirmePreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyMuestra, AccionDetalle.UNIRME))

@Preview(name = "Solicitar", showBackground = true, heightDp = 800)
@Composable
private fun DetalleSolicitarPreview() = PreviewDetalle(
    DetalleUiState.Contenido(lobbyMuestra.copy(esPrivado = true, requiereAprobacion = true), AccionDetalle.SOLICITAR),
)

@Preview(name = "Pendiente", showBackground = true, heightDp = 800)
@Composable
private fun DetallePendientePreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyMuestra, AccionDetalle.PENDIENTE))

@Preview(name = "Cupo lleno", showBackground = true, heightDp = 800)
@Composable
private fun DetalleCupoLlenoPreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyLleno, AccionDetalle.CUPO_LLENO))

@Preview(name = "Check-in", showBackground = true, heightDp = 800)
@Composable
private fun DetalleCheckInPreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyLleno, AccionDetalle.CHECK_IN))

@Preview(name = "En proceso", showBackground = true, heightDp = 800)
@Composable
private fun DetalleEnProcesoPreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyLleno, AccionDetalle.EN_PROCESO))

@Preview(name = "Finalizado", showBackground = true, heightDp = 800)
@Composable
private fun DetalleFinalizadoPreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyLleno, AccionDetalle.FINALIZADO))

@Preview(name = "Bloqueado", showBackground = true, heightDp = 800)
@Preview(name = "Bloqueado · Oscuro", showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetalleBloqueadoPreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyLleno, AccionDetalle.BLOQUEADO))

@Preview(name = "Menú Reportar", showBackground = true, heightDp = 800)
@Composable
private fun DetalleMenuPreview() =
    PreviewDetalle(DetalleUiState.Contenido(lobbyMuestra, AccionDetalle.UNIRME), menuAbierto = true)

@Preview(name = "Cargando", showBackground = true, heightDp = 800)
@Composable
private fun DetalleCargandoPreview() = PreviewDetalle(DetalleUiState.Cargando)

@Preview(name = "Error", showBackground = true, heightDp = 800)
@Preview(name = "Error · Oscuro", showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetalleErrorPreview() = PreviewDetalle(DetalleUiState.Error())

@Preview(name = "No encontrado", showBackground = true, heightDp = 800)
@Composable
private fun DetalleNoEncontradoPreview() = PreviewDetalle(DetalleUiState.Error(noEncontrado = true))

