package com.example.vamospues.ui.screens.moderacion

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.model.enums.EstadoAnalisis
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.components.EstadoChip
import com.example.vamospues.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoPublicacionScreen(
    estadoInicial: String = EstadoAnalisis.PENDIENTE_REVISION.name,
    onVolverExplorar: () -> Unit,
    onVerLobby: (String) -> Unit,
    onEditarLobby: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var estadoActualStr by remember(estadoInicial) {
        mutableStateOf(
            try {
                EstadoAnalisis.valueOf(estadoInicial)
            } catch (e: Exception) {
                EstadoAnalisis.PENDIENTE_REVISION
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Estado de publicación") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (com.example.vamospues.ui.components.Config.MODO_DEMO) {
                DemoEstadoSelector(
                    estados = listOf("Pendiente", "Aprobado", "Rechazado"),
                    estadoActual = when (estadoActualStr) {
                        EstadoAnalisis.PENDIENTE_REVISION -> "Pendiente"
                        EstadoAnalisis.APROBADO -> "Aprobado"
                        else -> "Rechazado"
                    },
                    onEstadoSelected = { sel ->
                        estadoActualStr = when (sel) {
                            "Pendiente" -> EstadoAnalisis.PENDIENTE_REVISION
                            "Aprobado" -> EstadoAnalisis.APROBADO
                            else -> EstadoAnalisis.RECHAZADO
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EstadoChip(text = "MODERACIÓN")
                    Spacer(modifier = Modifier.height(32.dp))

                    val (icon, iconTint, titulo, mensaje) = when (estadoActualStr) {
                        EstadoAnalisis.PENDIENTE_REVISION -> Quad(
                            Icons.Rounded.Security,
                            MaterialTheme.colorScheme.primary,
                            "En revisión",
                            "Estamos revisando tu plan para mantener una comunidad segura. Aún no es visible."
                        )
                        EstadoAnalisis.APROBADO -> Quad(
                            Icons.Rounded.Check,
                            MaterialTheme.colorScheme.tertiary,
                            "¡Lobby publicado!",
                            "Fútbol al caer la tarde ya está visible. Quedan 2 lugares para completar el plan."
                        )
                        else -> Quad(
                            Icons.Rounded.Warning,
                            MaterialTheme.colorScheme.error,
                            "Lobby rechazado",
                            "Motivo: la descripción promueve actividades que pueden causar daño."
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Lobby summary card
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Fútbol al caer la tarde",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hoy, 19:00 · Cancha Los Pinos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (estadoActualStr) {
                                    EstadoAnalisis.PENDIENTE_REVISION -> "Pendiente de revisión humana"
                                    EstadoAnalisis.APROBADO -> "Público · Principiante"
                                    else -> "No se publicó en Explorar"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val (textoBoton, onClickBoton) = when (estadoActualStr) {
                    EstadoAnalisis.PENDIENTE_REVISION -> "Volver a Explorar" to onVolverExplorar
                    EstadoAnalisis.APROBADO -> "Ver lobby" to { onVerLobby("lobby-1") }
                    else -> "Editar lobby" to { onEditarLobby("lobby-1") }
                }

                Button(
                    onClick = onClickBoton,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = textoBoton)
                }
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ResultadoPublicacionScreenPreview() {
    MyApplicationTheme {
        ResultadoPublicacionScreen(
            estadoInicial = EstadoAnalisis.PENDIENTE_REVISION.name,
            onVolverExplorar = {},
            onVerLobby = {},
            onEditarLobby = {},
            onBackClick = {}
        )
    }
}
