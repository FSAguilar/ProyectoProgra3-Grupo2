package com.example.vamospues.ui.screens.perfil

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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Usuario
import com.example.vamospues.model.enums.EstadoCuenta
import com.example.vamospues.model.enums.EstadoParticipacion
import com.example.vamospues.model.estado.Activo
import com.example.vamospues.model.estado.Bloqueado
import com.example.vamospues.model.estado.EnProceso
import com.example.vamospues.model.estado.Finalizado
import com.example.vamospues.model.estado.Lleno
import com.example.vamospues.model.estado.PendienteModeracion
import com.example.vamospues.ui.theme.MyApplicationTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

private val fechaHistorial = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
private val AltoEsqueleto = 112.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    state: PerfilUiState,
    onReintentar: () -> Unit,
    onAdmin: (() -> Unit)? = null,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    is PerfilUiState.Contenido -> ContenidoPerfil(state, onAdmin)
                    PerfilUiState.Cargando -> PerfilCargando()
                    PerfilUiState.Error -> PerfilError(onReintentar)
                }
            }
            selectorDemo?.invoke()
        }
    }
}

@Composable
private fun ContenidoPerfil(state: PerfilUiState.Contenido, onAdmin: (() -> Unit)?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp),
    ) {
        item {
            Identidad(state.usuario)
            Spacer(Modifier.height(16.dp))
            EstadoDeCuenta(state.estadoCuenta)
            Spacer(Modifier.height(24.dp))
            Text("Medallas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Medalla("Asistente\npuntual", Modifier.weight(1f))
                Medalla("Organizador\nconfiable", Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            Text("Historial de lobbies", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }
        if (state.historial.isEmpty()) {
            item {
                Text(
                    "Aún no has participado en un lobby.\nTus próximos planes aparecerán aquí.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.historial, key = { it.id }) { lobby ->
                LobbyHistorial(lobby, state.usuario.id)
                Spacer(Modifier.height(8.dp))
            }
        }
        if (onAdmin != null) {
            item {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAdmin, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Panel de administración")
                }
            }
        }
    }
}

@Composable
private fun Identidad(usuario: Usuario) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Outlined.AccountCircle, contentDescription = null,
            modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(0.dp))
        Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                "Karma ${usuario.reputacionKarma}",
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun EstadoDeCuenta(estado: EstadoCuenta) {
    val colores = MaterialTheme.colorScheme
    val (titulo, mensaje) = when (estado) {
        EstadoCuenta.ACTIVA -> "Cuenta activa" to
            "Puedes crear lobbies y participar en los planes de la comunidad."
        EstadoCuenta.SUSPENDIDA -> "Cuenta suspendida" to
            "No puedes crear ni unirte a lobbies mientras dure la suspensión."
        EstadoCuenta.BANEADA -> "Cuenta baneada" to
            "Tu acceso fue retirado por incumplir las normas de seguridad."
    }
    val activa = estado == EstadoCuenta.ACTIVA
    val color = if (activa) colores.tertiary else colores.error
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colores.secondaryContainer),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (activa) Icons.Outlined.Check else Icons.Outlined.Shield, null, tint = color)
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = color)
            }
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
        }
    }
}

@Composable
private fun Medalla(etiqueta: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.StarBorder, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LobbyHistorial(lobby: Lobby, usuarioId: String) {
    val participacion = lobby.participaciones.firstOrNull { it.participante.id == usuarioId }
    val estado = when (lobby.estadoActual) {
        is PendienteModeracion -> "Pendiente de moderación"
        is Activo -> "Activo"
        is Lleno -> "Lleno"
        is EnProceso -> "En proceso"
        is Finalizado -> "Finalizado"
        is Bloqueado -> "Bloqueado"
    }
    val rol = when {
        participacion?.checkInRealizado == true -> "Asististe"
        lobby.organizador.id == usuarioId -> "Organizaste"
        else -> when (participacion?.estado) {
            EstadoParticipacion.CONFIRMADA -> "Participación confirmada"
            EstadoParticipacion.PENDIENTE -> "Participación pendiente"
            EstadoParticipacion.RECHAZADA -> "Participación rechazada"
            EstadoParticipacion.CANCELADA -> "Participación cancelada"
            null -> null
        }
    }
    val fecha = lobby.fechaHora.format(fechaHistorial).trimEnd('.')
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Text(lobby.titulo, style = MaterialTheme.typography.titleMedium)
            Text(listOfNotNull(fecha, estado, rol).joinToString(" · "), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PerfilCargando() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 80.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(
            "Cargando tu perfil...",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        repeat(3) {
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).height(AltoEsqueleto),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {}
        }
    }
}

@Composable
private fun PerfilError(onReintentar: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Box(Modifier.weight(3f).fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Icon(Icons.Outlined.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    "No pudimos cargar tu perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Revisa la conexión. Tus datos no se han perdido.",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        OutlinedButton(onClick = onReintentar, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Refresh, null, Modifier.align(Alignment.CenterStart))
                Text("Reintentar")
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

internal fun perfilDe(usuario: Usuario): PerfilUiState.Contenido {
    val historial = FakeData.lobbies.filter { lobby ->
        lobby.organizador.id == usuario.id || lobby.participaciones.any { it.participante.id == usuario.id }
    }.sortedByDescending { it.fechaHora }
    return PerfilUiState.Contenido(usuario, historial)
}

@Composable
private fun PerfilPreview(state: PerfilUiState, admin: Boolean = false) {
    MyApplicationTheme {
        PerfilScreen(state, onReintentar = {}, onAdmin = if (admin) ({}) else null)
    }
}

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ActivaPreview() = PerfilPreview(perfilDe(FakeData.usuarioActual))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun SinHistorialPreview() = PerfilPreview(PerfilUiState.Contenido(FakeData.usuarioActual, emptyList()))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun SuspendidaPreview() = PerfilPreview(perfilDe(FakeData.usuarioSuspendido))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun BaneadaPreview() = PerfilPreview(perfilDe(FakeData.usuarioBaneado))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun CargandoPreview() = PerfilPreview(PerfilUiState.Cargando)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ErrorPreview() = PerfilPreview(PerfilUiState.Error)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun AdminPreview() = PerfilPreview(perfilDe(FakeData.administrador), admin = true)
