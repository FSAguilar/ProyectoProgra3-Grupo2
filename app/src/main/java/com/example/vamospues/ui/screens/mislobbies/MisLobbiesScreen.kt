package com.example.vamospues.ui.screens.mislobbies

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Usuario
import com.example.vamospues.ui.components.LobbyCard
import com.example.vamospues.ui.components.PantallaCargando
import com.example.vamospues.ui.components.PantallaError
import com.example.vamospues.ui.components.PantallaVacia
import com.example.vamospues.ui.theme.MyApplicationTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MisLobbiesTab(val etiqueta: String) {
    CREADOS("Creados"),
    PARTICIPACIONES("Participaciones"),
}

sealed interface MisLobbiesUiState {
    data class Contenido(
        val creados: List<Lobby>,
        val participaciones: List<Lobby>,
    ) : MisLobbiesUiState

    data object Cargando : MisLobbiesUiState
    data object Error : MisLobbiesUiState
}

internal fun misLobbiesDe(usuario: Usuario, lobbies: List<Lobby>): MisLobbiesUiState.Contenido {
    val creados = lobbies.filter { it.organizador.id == usuario.id }.distinctBy { it.id }
    val idsCreados = creados.mapTo(mutableSetOf()) { it.id }
    val participaciones = lobbies.filter { lobby ->
        lobby.id !in idsCreados && lobby.participaciones.any { it.participante.id == usuario.id }
    }.distinctBy { it.id }
    return MisLobbiesUiState.Contenido(creados, participaciones)
}

private val formatoFechaLobby = DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.forLanguageTag("es"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisLobbiesScreen(
    state: MisLobbiesUiState,
    tab: MisLobbiesTab,
    onTabSelected: (MisLobbiesTab) -> Unit,
    onLobbyClick: (String) -> Unit,
    onExplorar: () -> Unit,
    onReintentar: () -> Unit,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Mis lobbies", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = tab.ordinal) {
                MisLobbiesTab.entries.forEach { opcion ->
                    Tab(
                        selected = opcion == tab,
                        onClick = { onTabSelected(opcion) },
                        text = { Text(opcion.etiqueta) },
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    MisLobbiesUiState.Cargando -> PantallaCargando("Cargando tus lobbies...")
                    MisLobbiesUiState.Error -> PantallaError(
                        titulo = "No pudimos cargar tus lobbies",
                        mensaje = "Intenta nuevamente.",
                        onReintentarClick = onReintentar,
                    )
                    is MisLobbiesUiState.Contenido -> {
                        val lobbies = when (tab) {
                            MisLobbiesTab.CREADOS -> state.creados
                            MisLobbiesTab.PARTICIPACIONES -> state.participaciones
                        }
                        if (lobbies.isEmpty()) {
                            val (titulo, mensaje) = when (tab) {
                                MisLobbiesTab.CREADOS -> "Aún no creaste lobbies" to
                                    "Los lobbies que organices aparecerán aquí."
                                MisLobbiesTab.PARTICIPACIONES -> "Aún no participas en lobbies" to
                                    "Los lobbies a los que te unas aparecerán aquí."
                            }
                            PantallaVacia(
                                titulo = titulo,
                                mensaje = mensaje,
                                textoBoton = "Explorar lobbies",
                                onBotonClick = onExplorar,
                            )
                        } else {
                            ListaLobbies(lobbies, onLobbyClick)
                        }
                    }
                }
            }
            selectorDemo?.invoke()
        }
    }
}

@Composable
private fun ListaLobbies(lobbies: List<Lobby>, onLobbyClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(lobbies, key = { it.id }) { lobby ->
            val ocupados = lobby.cupoMaximo - lobby.cuposDisponibles()
            LobbyCard(
                titulo = lobby.titulo,
                subtitulo = "${lobby.categoria.nombre} · ${lobby.fechaHora.format(formatoFechaLobby)}",
                ubicacionTexto = lobby.ubicacion.nombreLugar,
                cuposTexto = "Quedan ${lobby.cuposDisponibles()} lugares · $ocupados/${lobby.cupoMaximo}",
                cuposProgress = ocupados.toFloat() / lobby.cupoMaximo.coerceAtLeast(1),
                nivelTexto = lobby.nivel.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { onLobbyClick(lobby.id) },
            )
        }
    }
}

@Composable
private fun MisLobbiesPreview(
    state: MisLobbiesUiState,
    tab: MisLobbiesTab = MisLobbiesTab.CREADOS,
) {
    MyApplicationTheme {
        MisLobbiesScreen(
            state = state,
            tab = tab,
            onTabSelected = {},
            onLobbyClick = {},
            onExplorar = {},
            onReintentar = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Preview(widthDp = 360, heightDp = 696, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ContenidoPreview() = MisLobbiesPreview(misLobbiesDe(FakeData.usuarioActual, FakeData.lobbies))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ParticipacionesVaciasPreview() = MisLobbiesPreview(
    misLobbiesDe(FakeData.usuarioActual, FakeData.lobbies),
    MisLobbiesTab.PARTICIPACIONES,
)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun CargandoPreview() = MisLobbiesPreview(MisLobbiesUiState.Cargando)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ErrorPreview() = MisLobbiesPreview(MisLobbiesUiState.Error)
