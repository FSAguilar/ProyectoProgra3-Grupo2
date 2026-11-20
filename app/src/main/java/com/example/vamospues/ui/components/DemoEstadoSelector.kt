package com.example.vamospues.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vamospues.Config
import com.example.vamospues.ui.theme.MyApplicationTheme

@Composable
fun DemoEstadoSelector(
    estados: List<String>,
    estadoActual: String,
    onEstadoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!Config.MODO_DEMO) return

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            estados.forEach { estado ->
                FilterChip(
                    selected = estado == estadoActual,
                    onClick = { onEstadoSelected(estado) },
                    label = { Text(text = estado) }
                )
            }
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Composable
fun DemoEstadoSelectorPreview() {
    MyApplicationTheme {
        DemoEstadoSelector(
            estados = listOf("Normal", "Vacío", "Cargando", "Error"),
            estadoActual = "Normal",
            onEstadoSelected = {}
        )
    }
}
