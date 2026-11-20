package com.example.vamospues.ui.screens.reportar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

/**
 * Hoja modal para reportar un lobby. No es una ruta: la abre el menú del detalle.
 * El fondo queda bloqueado por el scrim y la hoja no se cierra mientras se envía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarLobbySheet(
    state: ReportarUiState,
    onMotivoChange: (MotivoReporte) -> Unit,
    onDetalleChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onCerrar: () -> Unit,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    val enviando by rememberUpdatedState(state.enviando)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { valor -> valor != SheetValue.Hidden || !enviando },
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        ReportarLobbyContenido(
            state = state,
            onMotivoChange = onMotivoChange,
            onDetalleChange = onDetalleChange,
            onEnviar = onEnviar,
            onCerrar = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onCerrar()
                }
            },
            selectorDemo = selectorDemo,
        )
    }
}

/** Contenido sin estado propio de la hoja; se usa también en las previews. */
@Composable
fun ReportarLobbyContenido(
    state: ReportarUiState,
    onMotivoChange: (MotivoReporte) -> Unit,
    onDetalleChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        selectorDemo?.invoke()
        if (state.envio == EnvioReporte.ENVIADO) {
            ReporteEnviado(onCerrar)
        } else {
            FormularioReporte(state, onMotivoChange, onDetalleChange, onEnviar)
        }
    }
}

@Composable
private fun FormularioReporte(
    state: ReportarUiState,
    onMotivoChange: (MotivoReporte) -> Unit,
    onDetalleChange: (String) -> Unit,
    onEnviar: () -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        Text(
            text = "Reportar lobby",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() },
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Selecciona un motivo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.selectableGroup()) {
            MotivoReporte.entries.forEach { motivo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .selectable(
                            selected = state.motivo == motivo,
                            enabled = !state.enviando,
                            role = Role.RadioButton,
                            onClick = { onMotivoChange(motivo) },
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = state.motivo == motivo, onClick = null, enabled = !state.enviando)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = motivo.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.detalle,
            onValueChange = onDetalleChange,
            enabled = !state.enviando,
            label = { Text("Detalle (opcional)") },
            placeholder = { Text("Describe qué ocurrió") },
            minLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        if (state.envio == EnvioReporte.ERROR) {
            Text(
                text = "No se envió. Conservamos tu selección.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            Spacer(Modifier.height(16.dp))
        }
        if (state.enviando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }
        Button(
            onClick = onEnviar,
            enabled = state.puedeEnviar,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = when (state.envio) {
                    EnvioReporte.ENVIANDO -> "Enviando…"
                    EnvioReporte.ERROR -> "Reintentar envío"
                    else -> "Enviar reporte"
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tu reporte será revisado por moderación.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun ReporteEnviado(onCerrar: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text(
            text = "Reporte enviado",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 64.dp, horizontal = 16.dp)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Gracias por avisarnos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "El equipo de moderación revisará el contenido y tomará las medidas necesarias.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = onCerrar,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Cerrar", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---------- Previews ----------

@Composable
private fun PreviewReporte(state: ReportarUiState) {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ReportarLobbyContenido(
                state = state,
                onMotivoChange = {},
                onDetalleChange = {},
                onEnviar = {},
                onCerrar = {},
                selectorDemo = {
                    DemoEstadoSelector(
                        estados = VarianteReporte.entries.map { it.etiqueta },
                        estadoActual = VarianteReporte.de(state).etiqueta,
                        onEstadoSelected = {},
                    )
                },
            )
        }
    }
}

@Preview(name = "Sin selección", showBackground = true)
@Composable
private fun ReporteSinSeleccionPreview() = PreviewReporte(ReportarUiState())

@Preview(name = "Motivo", showBackground = true)
@Preview(name = "Motivo · Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReporteMotivoPreview() =
    PreviewReporte(ReportarUiState(motivo = MotivoReporte.ACTIVIDAD_PELIGROSA))

@Preview(name = "Enviando", showBackground = true)
@Composable
private fun ReporteEnviandoPreview() = PreviewReporte(
    ReportarUiState(motivo = MotivoReporte.ACTIVIDAD_PELIGROSA, envio = EnvioReporte.ENVIANDO),
)

@Preview(name = "Enviado", showBackground = true)
@Preview(name = "Enviado · Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReporteEnviadoPreview() = PreviewReporte(
    ReportarUiState(motivo = MotivoReporte.ACTIVIDAD_PELIGROSA, envio = EnvioReporte.ENVIADO),
)

@Preview(name = "Error", showBackground = true)
@Composable
private fun ReporteErrorPreview() = PreviewReporte(
    ReportarUiState(motivo = MotivoReporte.ACTIVIDAD_PELIGROSA, envio = EnvioReporte.ERROR),
)
