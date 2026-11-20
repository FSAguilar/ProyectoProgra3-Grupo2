package com.example.vamospues.ui.screens.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.Lobby
import com.example.vamospues.ui.components.EstadoChip
import com.example.vamospues.ui.theme.MyApplicationTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TamanoTarjetaQr = 232.dp
private val AltoTarjetaUbicacion = 144.dp
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")
private val formatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    lobbyId: String,
    state: CheckInUiState,
    segundosQr: Int,
    fechaDemo: LocalDate?,
    accesoPermitido: Boolean,
    onVolver: () -> Unit,
    onEscanear: () -> Unit,
    onValidarGps: () -> Unit,
    onRenovarCodigo: () -> Unit,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    val lobby = FakeData.lobbies.firstOrNull { it.id == lobbyId }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Check-in", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        bottomBar = {
            if (lobby != null && accesoPermitido && state != CheckInUiState.ORGANIZADOR) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state == CheckInUiState.VALIDANDO_GPS) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    when (state) {
                        CheckInUiState.QR_CADUCADO ->
                            Accion("Renovar código", onRenovarCodigo, Icons.Outlined.Refresh)
                        CheckInUiState.ASISTENCIA_CONFIRMADA ->
                            Accion("Volver al lobby", onVolver)
                        CheckInUiState.VALIDANDO_GPS ->
                            AccionGps("Validando...", false, onValidarGps)
                        CheckInUiState.FUERA_DEL_AREA ->
                            AccionGps("Validar por GPS", false, onValidarGps)
                        CheckInUiState.ERROR_GPS ->
                            AccionGps("Reintentar GPS", true, onValidarGps)
                        CheckInUiState.ASISTENTE ->
                            AccionGps("Validar por GPS", true, onValidarGps)
                        CheckInUiState.ORGANIZADOR -> Unit
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (lobby == null) {
                Text("No se encontró el lobby.", color = MaterialTheme.colorScheme.error)
            } else {
                ContextoLobby(lobby, fechaDemo ?: LocalDate.now())
                Spacer(Modifier.height(32.dp))
                if (accesoPermitido) {
                    when (state) {
                        CheckInUiState.ASISTENCIA_CONFIRMADA -> Confirmacion(lobby.titulo)
                        CheckInUiState.ORGANIZADOR, CheckInUiState.QR_CADUCADO ->
                            VistaOrganizador(state == CheckInUiState.QR_CADUCADO, segundosQr)
                        else -> VistaAsistente(state, onEscanear)
                    }
                    if (selectorDemo != null) {
                        Spacer(Modifier.height(32.dp))
                        selectorDemo()
                    }
                } else {
                    Text("El check-in está disponible el día del evento para personas confirmadas.")
                }
            }
        }
    }
}

@Composable
private fun ContextoLobby(lobby: Lobby, hoy: LocalDate) {
    val fecha = lobby.fechaHora
    val dia = if (fecha.toLocalDate() == hoy) "Hoy" else fecha.format(formatoFecha)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(lobby.titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            "$dia, ${fecha.format(formatoHora)} · ${lobby.ubicacion.nombreLugar}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VistaOrganizador(caducado: Boolean, segundosQr: Int) {
    EstadoChip(text = "ORGANIZADORA")
    Spacer(Modifier.height(32.dp))
    Card(
        modifier = Modifier.size(TamanoTarjetaQr),
        colors = CardDefaults.cardColors(
            containerColor = if (caducado) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Icon(
                if (caducado) Icons.Outlined.AccessTime else Icons.Outlined.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(if (caducado) 48.dp else 96.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (caducado) "Código caducado" else "QR de demostración",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
    Spacer(Modifier.height(32.dp))
    val tiempo = "%02d:%02d".format(Locale.ROOT, segundosQr / 60, segundosQr % 60)
    Text(
        if (caducado) "El código ya no es válido" else "Válido durante $tiempo",
        modifier = if (caducado) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier,
        style = MaterialTheme.typography.titleMedium,
        color = if (caducado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
    )
    Spacer(Modifier.height(24.dp))
    Text(
        "Muestra este código a las personas confirmadas al llegar al encuentro.",
        modifier = Modifier.padding(horizontal = 32.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun VistaAsistente(state: CheckInUiState, onEscanear: () -> Unit) {
    EstadoChip(text = "ASISTENTE CONFIRMADA")
    Spacer(Modifier.height(48.dp))
    Icon(
        Icons.Outlined.QrCodeScanner,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(24.dp))
    Text(
        "Confirma tu llegada",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(32.dp))
    Accion("Escanear QR", onEscanear, Icons.Outlined.QrCodeScanner)
    Spacer(Modifier.height(32.dp))
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = AltoTarjetaUbicacion),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
                Text("O valida tu ubicación", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                when (state) {
                    CheckInUiState.VALIDANDO_GPS -> "Comprobando tu distancia..."
                    CheckInUiState.ERROR_GPS -> "No se pudo obtener tu ubicación."
                    CheckInUiState.FUERA_DEL_AREA -> "Estás a 240 m · Fuera del área"
                    else -> "Estás a 35 m · Cerca del encuentro"
                },
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state == CheckInUiState.ERROR_GPS) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Distancia necesaria: 100 m o menos", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun Confirmacion(titulo: String) {
    Spacer(Modifier.height(112.dp))
    Icon(
        Icons.Outlined.CheckCircleOutline,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.tertiary,
    )
    Spacer(Modifier.height(32.dp))
    Text(
        "¡Llegada confirmada!",
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(24.dp))
    Text(
        "Tu asistencia a $titulo quedó registrada.",
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Accion(etiqueta: String, onClick: () -> Unit, icono: ImageVector? = null) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        ContenidoAccion(etiqueta, icono)
    }
}

@Composable
private fun AccionGps(etiqueta: String, habilitada: Boolean, onClick: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    if (habilitada) {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            ContenidoAccion(etiqueta, Icons.Outlined.MyLocation)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            ContenidoAccion(etiqueta, Icons.Outlined.MyLocation)
        }
    }
}

@Composable
private fun ContenidoAccion(etiqueta: String, icono: ImageVector?) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.align(Alignment.CenterStart).size(24.dp))
        }
        Text(etiqueta, modifier = Modifier.padding(horizontal = if (icono == null) 0.dp else 32.dp))
    }
}

@Composable
private fun CheckInPreview(state: CheckInUiState) {
    MyApplicationTheme {
        CheckInScreen(
            lobbyId = "lobby-1",
            state = state,
            segundosQr = 102,
            fechaDemo = LocalDate.of(2026, 12, 4),
            accesoPermitido = true,
            onVolver = {},
            onEscanear = {},
            onValidarGps = {},
            onRenovarCodigo = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun OrganizadorPreview() = CheckInPreview(CheckInUiState.ORGANIZADOR)

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AsistentePreview() = CheckInPreview(CheckInUiState.ASISTENTE)

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun QrCaducadoPreview() = CheckInPreview(CheckInUiState.QR_CADUCADO)

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ValidandoGpsPreview() = CheckInPreview(CheckInUiState.VALIDANDO_GPS)

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ErrorGpsPreview() = CheckInPreview(CheckInUiState.ERROR_GPS)

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun FueraDelAreaPreview() = CheckInPreview(CheckInUiState.FUERA_DEL_AREA)

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AsistenciaConfirmadaPreview() = CheckInPreview(CheckInUiState.ASISTENCIA_CONFIRMADA)
