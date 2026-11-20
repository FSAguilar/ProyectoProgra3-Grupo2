package com.example.vamospues.ui.screens.crearlobby

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
import com.example.vamospues.data.FakeData
import com.example.vamospues.ui.components.DemoEstadoSelector
import com.example.vamospues.ui.theme.MyApplicationTheme

enum class CrearLobbyVariant {
    DATOS, VALIDACION, PUBLICANDO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLobbyScreen(
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titulo by remember { mutableStateOf("Fútbol al caer la tarde") }
    var descripcion by remember { mutableStateOf("Una pichanga tranquila. Trae agua y ganas de jugar.") }
    var categoriaSeleccionada by remember { mutableStateOf(FakeData.categorias.firstOrNull()?.nombre ?: "Fútbol") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf("19 nov 2026") }
    var hora by remember { mutableStateOf("19:00") }
    var cupos by remember { mutableStateOf(10) }
    var esPrivado by remember { mutableStateOf(false) }
    var requiereAprobacion by remember { mutableStateOf(true) }
    var nombreLugar by remember { mutableStateOf("Cancha Los Pinos") }
    var direccion by remember { mutableStateOf("Av. Los Pinos 240, Lima") }
    var referencia by remember { mutableStateOf("Puerta 2, junto al kiosco") }

    var varianteDemo by remember { mutableStateOf(CrearLobbyVariant.DATOS) }
    var isPublishing by remember { mutableStateOf(false) }

    val tituloError = varianteDemo == CrearLobbyVariant.VALIDACION && titulo.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Crear lobby") },
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
                    estados = listOf("Datos", "Validación", "Publicando"),
                    estadoActual = when (varianteDemo) {
                        CrearLobbyVariant.DATOS -> if (esPrivado) "Privado" else "Datos"
                        CrearLobbyVariant.VALIDACION -> "Validación"
                        CrearLobbyVariant.PUBLICANDO -> "Publicando"
                    },
                    onEstadoSelected = { estado ->
                        varianteDemo = when (estado) {
                            "Datos" -> { esPrivado = false; CrearLobbyVariant.DATOS }
                            "Privado" -> { esPrivado = true; CrearLobbyVariant.DATOS }
                            "Validación" -> { titulo = ""; CrearLobbyVariant.VALIDACION }
                            else -> CrearLobbyVariant.PUBLICANDO
                        }
                    }
                )
            }

            if (varianteDemo == CrearLobbyVariant.PUBLICANDO || isPublishing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos del plan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(text = "Título") },
                    isError = tituloError,
                    supportingText = if (tituloError) {
                        { Text(text = "El título es obligatorio", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(text = "Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = { categoriaExpanded = !categoriaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = "Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        FakeData.categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    categoriaSeleccionada = cat.nombre
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text(text = "Fecha") },
                        trailingIcon = { Icon(Icons.Rounded.DateRange, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text(text = "Hora") },
                        trailingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                // Cupo máximo
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Cupo máximo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { if (cupos > 1) cupos-- },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text(text = "-", style = MaterialTheme.typography.headlineSmall)
                            }
                            Text(
                                text = cupos.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(
                                onClick = { cupos++ },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Acceso y ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Visibility Segmented / Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !esPrivado,
                        onClick = { esPrivado = false },
                        label = { Text(text = "Público") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = esPrivado,
                        onClick = { esPrivado = true },
                        label = { Text(text = "Privado") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (esPrivado) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Código (demo): VP-7K2M",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Requiere aprobación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = requiereAprobacion,
                        onCheckedChange = { requiereAprobacion = it }
                    )
                }

                // Ubicación block / Map preview placeholder
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Punto de encuentro",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Punto del encuentro",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = nombreLugar,
                    onValueChange = { nombreLugar = it },
                    label = { Text(text = "Nombre del lugar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text(text = "Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = referencia,
                    onValueChange = { referencia = it },
                    label = { Text(text = "Referencia") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Se revisará antes de publicarse.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                val enabled = varianteDemo != CrearLobbyVariant.VALIDACION && !isPublishing
                Button(
                    onClick = {
                        if (titulo.isNotBlank()) {
                            isPublishing = true
                            onPublishSuccess()
                        } else {
                            varianteDemo = CrearLobbyVariant.VALIDACION
                        }
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = if (isPublishing || varianteDemo == CrearLobbyVariant.PUBLICANDO) "Publicando..." else "Publicar")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun CrearLobbyScreenPreview() {
    MyApplicationTheme {
        CrearLobbyScreen(
            onBackClick = {},
            onPublishSuccess = {}
        )
    }
}
