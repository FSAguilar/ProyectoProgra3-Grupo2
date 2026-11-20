package com.example.vamospues.ui.screens.chat

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.ui.theme.MyApplicationTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")
private val AltoEsqueleto = 112.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    tituloLobby: String,
    confirmados: Int,
    minimoConfirmados: Int,
    borrador: String,
    onBorradorChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onVotar: (encuestaId: String, opcionId: String) -> Unit,
    onCompartirUbicacion: () -> Unit,
    onReintentar: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    selectorDemo: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Chat del lobby", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        snackbarHost = { if (snackbarHostState != null) SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
                when (state) {
                    ChatUiState.Cargando -> ChatCargando()
                    ChatUiState.Error -> ChatError(onReintentar)
                    ChatUiState.Bloqueado -> {
                        Banner(tituloLobby, "$confirmados confirmados · Se activa con $minimoConfirmados", Icons.Outlined.Lock)
                        MensajeCentral(
                            Icons.Outlined.Lock,
                            "Aún no se activa",
                            "El chat estará disponible cuando el lobby alcance $minimoConfirmados personas confirmadas.",
                        )
                    }
                    ChatUiState.SinMensajes -> {
                        BannerDisponible(tituloLobby, confirmados, minimoConfirmados)
                        MensajeCentral(
                            Icons.AutoMirrored.Outlined.Chat,
                            "Todo listo para conversar",
                            "Sé la primera persona en escribir. Coordinen aquí los detalles del encuentro.",
                            modifier = Modifier.weight(1f),
                        )
                        EntradaMensaje(false, borrador, onBorradorChange, onEnviar, onCompartirUbicacion)
                    }
                    is ChatUiState.Conversacion -> {
                        BannerDisponible(tituloLobby, confirmados, minimoConfirmados)
                        Conversacion(
                            state.mensajes, state.encuestas, false, onVotar,
                            modifier = Modifier.weight(1f),
                        )
                        EntradaMensaje(false, borrador, onBorradorChange, onEnviar, onCompartirUbicacion)
                    }
                    is ChatUiState.SoloLectura -> {
                        Banner("Solo lectura · Lobby finalizado", "Puedes consultar la conversación", Icons.Outlined.Lock)
                        Conversacion(
                            state.mensajes, state.encuestas, true, onVotar,
                            modifier = Modifier.weight(1f),
                        )
                        EntradaMensaje(true, "", onBorradorChange, onEnviar, onCompartirUbicacion)
                    }
                }
            }
            selectorDemo?.invoke()
        }
    }
}

@Composable
private fun BannerDisponible(titulo: String, confirmados: Int, minimo: Int) {
    Banner(titulo, "$confirmados confirmados · Mínimo: $minimo", Icons.Outlined.Groups)
}

@Composable
private fun Banner(titulo: String, texto: String, icono: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(texto, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun Conversacion(
    mensajes: List<MensajeChatUi>,
    encuestas: List<EncuestaChatUi>,
    soloLectura: Boolean,
    onVotar: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horaInicial = mensajes.firstOrNull()?.hora
    val lista = rememberLazyListState()
    var cantidadAnterior by remember { mutableIntStateOf(mensajes.size) }
    LaunchedEffect(mensajes.size) {
        if (mensajes.size > cantidadAnterior) lista.animateScrollToItem(encuestas.size)
        cantidadAnterior = mensajes.size
    }
    LazyColumn(
        state = lista,
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(encuestas.asReversed(), key = { "encuesta-${it.id}" }) {
            Encuesta(it, soloLectura, onVotar)
        }
        items(mensajes.asReversed(), key = { "mensaje-${it.id}" }) { Burbuja(it) }
        if (horaInicial != null) {
            item(key = "dia") {
                Text(
                    "HOY · ${horaInicial.format(formatoHora)}",
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Burbuja(mensaje: MensajeChatUi) {
    val colores = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth(), contentAlignment = if (mensaje.esPropio) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (mensaje.esPropio) 0.83f else 0.9f),
            color = if (mensaje.esPropio) colores.primaryContainer else colores.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (mensaje.esPropio) "${mensaje.autor} · ${mensaje.hora.format(formatoHora)}" else mensaje.autor,
                    style = MaterialTheme.typography.labelSmall,
                    color = colores.primary,
                )
                Text(mensaje.texto, style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Encuesta(encuesta: EncuestaChatUi, soloLectura: Boolean, onVotar: (String, String) -> Unit) {
    val total = encuesta.opciones.sumOf { it.votos }
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colores.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp).selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(encuesta.pregunta, style = MaterialTheme.typography.titleMedium)
            Text(
                if (soloLectura) "Encuesta cerrada · $total votos" else "Encuesta rápida · Elige una opción",
                style = MaterialTheme.typography.labelSmall,
            )
            encuesta.opciones.forEach { opcion ->
                val seleccionada = encuesta.opcionSeleccionadaId == opcion.id
                val avance = if (total > 0) opcion.votos.toFloat() / total else 0f
                val color = if (seleccionada) colores.primary else colores.outline
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
                        selected = seleccionada,
                        enabled = !soloLectura,
                        role = Role.RadioButton,
                        onClick = { onVotar(encuesta.id, opcion.id) },
                    ),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (seleccionada) {
                            Icon(Icons.Outlined.Check, null, Modifier.size(16.dp), tint = color)
                        }
                        Text(
                            opcion.texto,
                            modifier = Modifier.weight(1f).padding(start = if (seleccionada) 4.dp else 0.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (seleccionada) colores.primary else colores.onSurfaceVariant,
                        )
                        Text(
                            if (opcion.votos == 1) "1 voto" else "${opcion.votos} votos",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { avance },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = color,
                        trackColor = colores.secondaryContainer,
                        drawStopIndicator = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun EntradaMensaje(
    soloLectura: Boolean,
    borrador: String,
    onBorradorChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onCompartirUbicacion: () -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = colores.secondaryContainer,
    ) {
        Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCompartirUbicacion, enabled = !soloLectura) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = "Compartir ubicación",
                    tint = if (soloLectura) colores.outline else colores.primary,
                )
            }
            if (soloLectura) {
                Text("Solo lectura", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            } else {
                BasicTextField(
                    value = borrador,
                    onValueChange = onBorradorChange,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        .semantics { contentDescription = "Mensaje" },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colores.onSurface),
                    cursorBrush = SolidColor(colores.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (borrador.isNotBlank()) onEnviar() }),
                    decorationBox = { campo ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (borrador.isEmpty()) Text("Mensaje", color = colores.onSurfaceVariant)
                            campo()
                        }
                    },
                )
            }
            IconButton(onClick = onEnviar, enabled = !soloLectura && borrador.isNotBlank()) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Enviar mensaje",
                    tint = if (soloLectura) colores.outline else colores.primary,
                )
            }
        }
    }
}

@Composable
private fun MensajeCentral(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().fillMaxSize().padding(bottom = 64.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icono, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text(titulo, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ChatCargando() {
    Column(
        Modifier.fillMaxSize().padding(top = 80.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(
            "Cargando conversación...",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        repeat(3) {
            Surface(
                Modifier.fillMaxWidth().weight(1f, fill = false).height(AltoEsqueleto),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {}
        }
    }
}

@Composable
private fun ChatError(onReintentar: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        MensajeCentral(
            Icons.Outlined.ErrorOutline,
            "Chat no disponible",
            "No pudimos cargar los mensajes. Vuelve a intentarlo.",
            modifier = Modifier.weight(3f),
        )
        OutlinedButton(onClick = onReintentar, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Refresh, null, Modifier.align(Alignment.CenterStart))
                Text("Reintentar")
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

internal val mensajesChatDemo = listOf(
    MensajeChatUi("llegada", "Lucía · organizadora", "¡Nos vemos en la puerta 2!\nEstá junto al kiosco.", LocalTime.of(18, 20), false),
    MensajeChatUi("respuesta", "Tú", "¡Perfecto! Llego en 10 minutos.", LocalTime.of(18, 22), true),
)

internal val encuestaChatDemo = EncuestaChatUi(
    "equipo",
    "¿Qué llevamos para el equipo?",
    listOf(
        OpcionEncuestaUi("balon", "Balón", 3),
        OpcionEncuestaUi("petos", "Petos", 2),
        OpcionEncuestaUi("ambos", "Ambos", 3),
    ),
    opcionSeleccionadaId = "ambos",
)

@Composable
private fun ChatPreview(state: ChatUiState) {
    MyApplicationTheme {
        ChatScreen(
            state = state,
            tituloLobby = "Fútbol al caer la tarde",
            confirmados = if (state == ChatUiState.Bloqueado) 4 else 8,
            minimoConfirmados = 6,
            borrador = "",
            onBorradorChange = {},
            onEnviar = {},
            onVotar = { _, _ -> },
            onCompartirUbicacion = {},
            onReintentar = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ConversacionPreview() = ChatPreview(ChatUiState.Conversacion(mensajesChatDemo, listOf(encuestaChatDemo)))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun SinMensajesPreview() = ChatPreview(ChatUiState.SinMensajes)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun BloqueadoPreview() = ChatPreview(ChatUiState.Bloqueado)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun SoloLecturaPreview() = ChatPreview(ChatUiState.SoloLectura(mensajesChatDemo, listOf(encuestaChatDemo)))

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun CargandoPreview() = ChatPreview(ChatUiState.Cargando)

@Preview(widthDp = 360, heightDp = 696, showBackground = true)
@Composable
private fun ErrorPreview() = ChatPreview(ChatUiState.Error)
