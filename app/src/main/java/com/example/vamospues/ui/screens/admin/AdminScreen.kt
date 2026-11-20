package com.example.vamospues.ui.screens.admin

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.Administrador
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Reporte
import com.example.vamospues.model.Usuario
import com.example.vamospues.model.enums.AccionModeracion
import com.example.vamospues.model.enums.EstadoReporte
import com.example.vamospues.model.moderacion.Filtro
import com.example.vamospues.model.moderacion.FiltroIA
import com.example.vamospues.model.moderacion.FiltroPalabrasProhibidas
import com.example.vamospues.model.moderacion.FiltroRevisionHumana
import com.example.vamospues.ui.components.EstadoChip
import com.example.vamospues.ui.theme.MyApplicationTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("es"))
private val altoBoton = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    state: AdminUiState,
    administrador: Administrador,
    tab: AdminTab,
    vista: AdminVista,
    estadosReportes: Map<String, EstadoReporte>,
    filtrosActivos: Map<String, Boolean>,
    palabras: String,
    palabrasModificadas: Boolean,
    motivoRechazo: String,
    onTabSelected: (AdminTab) -> Unit,
    onBack: () -> Unit,
    onReporteSelected: (String) -> Unit,
    onAccionReporte: (String, AccionModeracion) -> Unit,
    onAprobarLobby: (String) -> Unit,
    onRechazarLobby: (String) -> Unit,
    onMotivoRechazoChange: (String) -> Unit,
    onConfirmarRechazo: (String) -> Unit,
    onCancelarRechazo: () -> Unit,
    onFiltroActivoChange: (String, Boolean) -> Unit,
    onPalabrasChange: (String) -> Unit,
    onGuardarPalabras: () -> Unit,
    onReintentar: () -> Unit,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    val titulo = if (vista is AdminVista.DetalleReporte) "Detalle del reporte" else "Administración"
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(titulo, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(
                selectedTabIndex = tab.ordinal,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                AdminTab.entries.forEach { opcion ->
                    Tab(
                        selected = opcion == tab,
                        onClick = { onTabSelected(opcion) },
                        modifier = Modifier.heightIn(min = altoBoton),
                        text = { Text(opcion.etiqueta, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    AdminUiState.Cargando -> AdminCargando()
                    AdminUiState.Error -> AdminError(onReintentar)
                    is AdminUiState.Contenido -> when (vista) {
                        AdminVista.Panel -> PanelAdmin(
                            state = state,
                            tab = tab,
                            estadosReportes = estadosReportes,
                            filtrosActivos = filtrosActivos,
                            palabras = palabras,
                            palabrasModificadas = palabrasModificadas,
                            onReporteSelected = onReporteSelected,
                            onAprobarLobby = onAprobarLobby,
                            onRechazarLobby = onRechazarLobby,
                            onFiltroActivoChange = onFiltroActivoChange,
                            onPalabrasChange = onPalabrasChange,
                            onGuardarPalabras = onGuardarPalabras,
                        )
                        is AdminVista.DetalleReporte -> {
                            val reporte = state.reportes.firstOrNull { it.id == vista.reporteId }
                            if (reporte == null) AdminElementoNoDisponible("El reporte ya no está disponible.")
                            else DetalleReporte(
                                reporte = reporte,
                                administrador = administrador,
                                estado = estadosReportes[reporte.id] ?: reporte.estado,
                                onAccion = { onAccionReporte(reporte.id, it) },
                            )
                        }
                        is AdminVista.RechazoLobby -> {
                            val lobby = state.lobbiesPendientes.firstOrNull { it.id == vista.lobbyId }
                            if (lobby == null) AdminElementoNoDisponible("El lobby ya no está disponible.")
                            else RechazarPublicacion(
                                lobby = lobby,
                                motivo = motivoRechazo,
                                onMotivoChange = onMotivoRechazoChange,
                                onConfirmar = { onConfirmarRechazo(lobby.id) },
                                onCancelar = onCancelarRechazo,
                            )
                        }
                    }
                }
            }
            selectorDemo?.invoke()
        }
    }
}

@Composable
private fun PanelAdmin(
    state: AdminUiState.Contenido,
    tab: AdminTab,
    estadosReportes: Map<String, EstadoReporte>,
    filtrosActivos: Map<String, Boolean>,
    palabras: String,
    palabrasModificadas: Boolean,
    onReporteSelected: (String) -> Unit,
    onAprobarLobby: (String) -> Unit,
    onRechazarLobby: (String) -> Unit,
    onFiltroActivoChange: (String, Boolean) -> Unit,
    onPalabrasChange: (String) -> Unit,
    onGuardarPalabras: () -> Unit,
) {
    when (tab) {
        AdminTab.REPORTES -> ReportesPanel(state.reportes, estadosReportes, onReporteSelected)
        AdminTab.REVISION -> RevisionPanel(state.lobbiesPendientes, onAprobarLobby, onRechazarLobby)
        AdminTab.FILTROS -> FiltrosPanel(
            state.filtros,
            filtrosActivos,
            palabras,
            palabrasModificadas,
            onFiltroActivoChange,
            onPalabrasChange,
            onGuardarPalabras,
        )
    }
}

@Composable
private fun ReportesPanel(
    reportes: List<Reporte>,
    estadosReportes: Map<String, EstadoReporte>,
    onReporteSelected: (String) -> Unit,
) {
    if (reportes.isEmpty()) {
        AdminVacio("Sin reportes pendientes", "No hay contenido pendiente en esta pestaña.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text("Reportes de la comunidad", style = MaterialTheme.typography.titleMedium) }
        items(reportes, key = { it.id }) { reporte ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onReporteSelected(reporte.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tituloReportado(reporte), style = MaterialTheme.typography.titleMedium)
                    Text(reporte.motivo, style = MaterialTheme.typography.bodyMedium)
                    EstadoChip(textoEstado(estadosReportes[reporte.id] ?: reporte.estado))
                }
            }
        }
    }
}

@Composable
private fun DetalleReporte(
    reporte: Reporte,
    administrador: Administrador,
    estado: EstadoReporte,
    onAccion: (AccionModeracion) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { EstadoChip(textoEstado(estado)) }
        item { Text(tituloReportado(reporte), style = MaterialTheme.typography.headlineSmall) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Motivo: ${reporte.motivo}", style = MaterialTheme.typography.bodyMedium)
                    Text("Fecha: ${reporte.fecha.format(formatoFecha)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Revisión a cargo de ${administrador.nombre}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item { Text("Acciones de moderación", style = MaterialTheme.typography.titleMedium) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AccionButton("Descartar", Modifier.weight(1f)) { onAccion(AccionModeracion.DESCARTAR) }
                    AccionButton("Advertir", Modifier.weight(1f)) { onAccion(AccionModeracion.ADVERTIR) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AccionButton("Suspender", Modifier.weight(1f)) { onAccion(AccionModeracion.SUSPENDER) }
                    TextButton(
                        onClick = { onAccion(AccionModeracion.BANEAR) },
                        modifier = Modifier.weight(1f).heightIn(min = altoBoton),
                    ) { Text("Banear", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(
                    onClick = { onAccion(AccionModeracion.ELIMINAR_CONTENIDO) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = altoBoton),
                ) { Text("Eliminar contenido", color = MaterialTheme.colorScheme.error) }
            }
        }
        item {
            Text(
                "Las medidas actualizan esta demostración sin modificar el dominio.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccionButton(texto: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = altoBoton)) { Text(texto) }
}

@Composable
private fun RevisionPanel(
    lobbies: List<Lobby>,
    onAprobar: (String) -> Unit,
    onRechazar: (String) -> Unit,
) {
    if (lobbies.isEmpty()) {
        AdminVacio("Sin publicaciones pendientes", "No hay lobbies esperando revisión humana.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { Text("Esperan revisión humana", style = MaterialTheme.typography.titleMedium) }
        items(lobbies, key = { it.id }) { lobby ->
            val analisis = lobby.historialAnalisis.last()
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(lobby.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${lobby.organizador.nombre} · ${lobby.fechaHora.format(formatoFecha)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    EstadoChip("Riesgo ${nivelRiesgo(analisis.nivelRiesgo)} · IA")
                    Text("Pendiente de evaluación humana.", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { onAprobar(lobby.id) },
                            modifier = Modifier.weight(1f).heightIn(min = altoBoton),
                        ) { Text("Aprobar") }
                        OutlinedButton(
                            onClick = { onRechazar(lobby.id) },
                            modifier = Modifier.weight(1f).heightIn(min = altoBoton),
                        ) { Text("Rechazar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RechazarPublicacion(
    lobby: Lobby,
    motivo: String,
    onMotivoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text("Rechazar publicación", style = MaterialTheme.typography.headlineSmall) }
        item { Text(lobby.titulo, style = MaterialTheme.typography.titleMedium) }
        item {
            Text(
                "La persona organizadora recibirá este motivo. Sé claro y específico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedTextField(
                value = motivo,
                onValueChange = onMotivoChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 144.dp),
                label = { Text("Motivo obligatorio") },
                supportingText = {
                    if (motivo.isBlank()) Text("Escribe un motivo antes de rechazar.")
                },
                isError = motivo.isBlank(),
                minLines = 5,
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
        item {
            Button(
                onClick = onConfirmar,
                enabled = motivo.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = altoBoton),
            ) { Text("Rechazar lobby") }
        }
        item {
            TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth().heightIn(min = altoBoton)) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun FiltrosPanel(
    filtros: List<Filtro>,
    filtrosActivos: Map<String, Boolean>,
    palabras: String,
    palabrasModificadas: Boolean,
    onFiltroActivoChange: (String, Boolean) -> Unit,
    onPalabrasChange: (String) -> Unit,
    onGuardarPalabras: () -> Unit,
) {
    if (filtros.isEmpty()) {
        AdminVacio("Sin filtros configurados", "No hay políticas de moderación disponibles.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { Text("Filtros de moderación", style = MaterialTheme.typography.titleMedium) }
        items(filtros, key = { it.id }) { filtro ->
            FiltroItem(
                filtro = filtro,
                activo = filtrosActivos[filtro.id] ?: filtro.estaActivo(),
                onActivoChange = { onFiltroActivoChange(filtro.id, it) },
            )
        }
        if (filtros.any { it is FiltroPalabrasProhibidas }) {
            item {
                OutlinedTextField(
                    value = palabras,
                    onValueChange = onPalabrasChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lista de palabras") },
                    supportingText = { Text("Un término por línea.") },
                    minLines = 4,
                )
            }
            item {
                Button(
                    onClick = onGuardarPalabras,
                    enabled = palabrasModificadas && palabras.lineSequence().any { it.isNotBlank() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = altoBoton),
                ) { Text("Guardar palabras") }
            }
        }
    }
}

@Composable
private fun FiltroItem(filtro: Filtro, activo: Boolean, onActivoChange: (Boolean) -> Unit) {
    val descripcion = when (filtro) {
        is FiltroPalabrasProhibidas -> "Bloqueo por coincidencia de términos"
        is FiltroIA -> "Análisis automático de riesgo"
        is FiltroRevisionHumana -> "Cola de evaluación por moderadores"
        else -> "Política de moderación"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(filtro.nombre, style = MaterialTheme.typography.titleMedium)
            Text(descripcion, style = MaterialTheme.typography.labelSmall)
        }
        Switch(
            checked = activo,
            onCheckedChange = onActivoChange,
            modifier = Modifier.heightIn(min = altoBoton),
        )
    }
}

@Composable
private fun AdminCargando() {
    Column(
        Modifier.fillMaxSize().padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        Text("Cargando el panel...", style = MaterialTheme.typography.bodyMedium)
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth().height(112.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {}
        }
    }
}

@Composable
private fun AdminVacio(titulo: String, mensaje: String) {
    AdminMensajeCentral(Icons.Outlined.Shield, titulo, mensaje)
}

@Composable
private fun AdminError(onReintentar: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminMensajeCentral(
            Icons.Outlined.ErrorOutline,
            "No se pudo cargar",
            "No se aplicó ningún cambio. Intenta de nuevo.",
            Modifier.weight(1f),
        )
        OutlinedButton(onClick = onReintentar, modifier = Modifier.fillMaxWidth().heightIn(min = altoBoton)) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Reintentar")
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun AdminElementoNoDisponible(mensaje: String) {
    AdminMensajeCentral(Icons.Outlined.ErrorOutline, "Contenido no disponible", mensaje)
}

@Composable
private fun AdminMensajeCentral(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icono, contentDescription = null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun tituloReportado(reporte: Reporte): String = when (val reportado = reporte.reportado) {
    is Lobby -> "Lobby · ${reportado.titulo}"
    is Usuario -> "Usuario · ${reportado.nombre}"
    else -> "Contenido · ${reportado.obtenerId()}"
}

private fun textoEstado(estado: EstadoReporte): String = when (estado) {
    EstadoReporte.PENDIENTE -> "Pendiente"
    EstadoReporte.RESUELTO -> "Resuelto"
    EstadoReporte.DESCARTADO -> "Descartado"
}

internal fun nivelRiesgo(nivel: Int): String = when {
    nivel <= 0 -> "bajo"
    nivel == 1 -> "medio"
    else -> "alto"
}

@Composable
private fun AdminPreview(
    state: AdminUiState,
    tab: AdminTab = AdminTab.REPORTES,
    vista: AdminVista = AdminVista.Panel,
    motivo: String = "",
) {
    val filtroPalabras = FakeData.filtros.filterIsInstance<FiltroPalabrasProhibidas>().firstOrNull()
    MyApplicationTheme {
        AdminScreen(
            state = state,
            administrador = FakeData.administrador,
            tab = tab,
            vista = vista,
            estadosReportes = emptyMap(),
            filtrosActivos = FakeData.filtros.associate { it.id to it.estaActivo() },
            palabras = filtroPalabras?.palabrasProhibidas?.joinToString("\n").orEmpty(),
            palabrasModificadas = false,
            motivoRechazo = motivo,
            onTabSelected = {},
            onBack = {},
            onReporteSelected = {},
            onAccionReporte = { _, _ -> },
            onAprobarLobby = {},
            onRechazarLobby = {},
            onMotivoRechazoChange = {},
            onConfirmarRechazo = {},
            onCancelarRechazo = {},
            onFiltroActivoChange = { _, _ -> },
            onPalabrasChange = {},
            onGuardarPalabras = {},
            onReintentar = {},
        )
    }
}

private fun contenidoPreview() = contenidoAdmin(FakeData.reportes, FakeData.lobbies, FakeData.filtros)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ReportesPreview() = AdminPreview(contenidoPreview())

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun DetallePreview() = AdminPreview(
    contenidoPreview(),
    vista = AdminVista.DetalleReporte(FakeData.reportes.first().id),
)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun RevisionPreview() = AdminPreview(contenidoPreview(), tab = AdminTab.REVISION)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun RechazoPreview() = AdminPreview(
    contenidoPreview(),
    tab = AdminTab.REVISION,
    vista = AdminVista.RechazoLobby("lobby-8"),
)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun FiltrosPreview() = AdminPreview(contenidoPreview(), tab = AdminTab.FILTROS)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun VacioPreview() = AdminPreview(AdminUiState.Contenido(emptyList(), emptyList(), emptyList()))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun CargandoPreview() = AdminPreview(AdminUiState.Cargando)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Preview(widthDp = 360, heightDp = 696, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ErrorPreview() = AdminPreview(AdminUiState.Error)
