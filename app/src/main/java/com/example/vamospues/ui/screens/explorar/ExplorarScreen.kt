package com.example.vamospues.ui.screens.explorar

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.Config
import com.example.vamospues.data.FakeData
import com.example.vamospues.model.Lobby
import com.example.vamospues.model.estado.Activo
import com.example.vamospues.ui.components.*
import com.example.vamospues.ui.theme.MyApplicationTheme
import java.time.format.DateTimeFormatter

enum class ExplorarUiVariant {
    CONTENIDO, SIN_RESULTADOS, CARGANDO, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorarScreen(
    lobbies: List<Lobby> = FakeData.lobbies,
    onLobbyClick: (String) -> Unit,
    onCreateLobbyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoriaSeleccionada by remember { mutableStateOf<String?>("Fútbol") }
    var radioSeleccionado by remember { mutableStateOf("5 km") }
    var varianteDemo by remember { mutableStateOf(ExplorarUiVariant.CONTENIDO) }

    val categorias = listOf("Fútbol", "Vóley", "Cine", "Juegos")
    val radios = listOf("2 km", "5 km", "10 km")

    // Filter public & active lobbies
    val publicLobbies = remember(lobbies, categoriaSeleccionada) {
        lobbies.filter { lobby ->
            val esPublicoActivo = lobby.esPublico() && lobby.estadoActual is Activo
            val coincideCategoria = categoriaSeleccionada == null ||
                    lobby.categoria.nombre.equals(categoriaSeleccionada, ignoreCase = true)
            esPublicoActivo && coincideCategoria
        }
    }

    val effectiveVariant = if (varianteDemo == ExplorarUiVariant.CONTENIDO && publicLobbies.isEmpty()) {
        ExplorarUiVariant.SIN_RESULTADOS
    } else {
        varianteDemo
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "VamosPues") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateLobbyClick,
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Crear lobby") },
                text = { Text(text = "Crear lobby") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (Config.MODO_DEMO) {
                DemoEstadoSelector(
                    estados = listOf("Contenido", "Sin resultados", "Cargando", "Error"),
                    estadoActual = when (varianteDemo) {
                        ExplorarUiVariant.CONTENIDO -> "Contenido"
                        ExplorarUiVariant.SIN_RESULTADOS -> "Sin resultados"
                        ExplorarUiVariant.CARGANDO -> "Cargando"
                        ExplorarUiVariant.ERROR -> "Error"
                    },
                    onEstadoSelected = { estado ->
                        varianteDemo = when (estado) {
                            "Contenido" -> ExplorarUiVariant.CONTENIDO
                            "Sin resultados" -> ExplorarUiVariant.SIN_RESULTADOS
                            "Cargando" -> ExplorarUiVariant.CARGANDO
                            else -> ExplorarUiVariant.ERROR
                        }
                    }
                )
            }

            when (effectiveVariant) {
                ExplorarUiVariant.CARGANDO -> {
                    PantallaCargando(mensaje = "Buscando lobbies cercanos...")
                }
                ExplorarUiVariant.ERROR -> {
                    PantallaError(
                        titulo = "No pudimos cargar",
                        mensaje = "Revisa tu conexión y vuelve a intentarlo.",
                        textoReintentar = "Reintentar",
                        onReintentarClick = { varianteDemo = ExplorarUiVariant.CONTENIDO }
                    )
                }
                ExplorarUiVariant.SIN_RESULTADOS -> {
                    PantallaVacia(
                        titulo = "Sin lobbies cerca",
                        mensaje = "No hay planes con estos filtros. Prueba un radio de 10 km.",
                        textoBoton = "Crear lobby",
                        onBotonClick = onCreateLobbyClick
                    )
                }
                ExplorarUiVariant.CONTENIDO -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Encuentra tu próximo plan",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Category filter chips
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categorias.forEach { cat ->
                                    FilterChip(
                                        selected = categoriaSeleccionada.equals(cat, ignoreCase = true),
                                        onClick = {
                                            categoriaSeleccionada = if (categoriaSeleccionada.equals(cat, ignoreCase = true)) null else cat
                                        },
                                        label = { Text(text = cat) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Radius selector row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                radios.forEach { radio ->
                                    FilterChip(
                                        selected = radioSeleccionado == radio,
                                        onClick = { radioSeleccionado = radio },
                                        label = { Text(text = radio) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "CERCA DE TI · SOLO LOBBIES PÚBLICOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(publicLobbies) { lobby ->
                            val formatter = DateTimeFormatter.ofPattern("HH:mm")
                            val fechaStr = lobby.fechaHora.format(formatter)
                            val subtitulo = "${lobby.categoria.nombre} · Hoy, $fechaStr"
                            val distanciaDemo = if (lobby.id == "lobby-1") "1,2 km" else "3,4 km"
                            val ubicacionTexto = "${lobby.ubicacion.nombreLugar} · $distanciaDemo"
                            val cuposOcupados = lobby.cupoMaximo - lobby.cuposDisponibles()
                            val cuposTexto = "Quedan ${lobby.cuposDisponibles()} lugares · $cuposOcupados/${lobby.cupoMaximo}"
                            val cuposProgress = cuposOcupados.toFloat() / lobby.cupoMaximo.toFloat()

                            LobbyCard(
                                titulo = lobby.titulo,
                                subtitulo = subtitulo,
                                ubicacionTexto = ubicacionTexto,
                                cuposTexto = cuposTexto,
                                cuposProgress = cuposProgress,
                                nivelTexto = lobby.nivel.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                onClick = { onLobbyClick(lobby.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Contenido Claro", showBackground = true)
@Preview(name = "Contenido Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ExplorarScreenPreview() {
    MyApplicationTheme {
        ExplorarScreen(
            onLobbyClick = {},
            onCreateLobbyClick = {}
        )
    }
}
