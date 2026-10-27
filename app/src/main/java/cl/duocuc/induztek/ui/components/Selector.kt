package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun Selector(
    etiqueta: String,
    valor: String,
    opciones: List<Pair<String, String>>,
    cambio: (String) -> Unit,
    error: String? = null,
) {
    var expandido by remember { mutableStateOf(false) }
    Column {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color =
                if (error != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
        )
        Box {
            OutlinedButton(
                onClick = { expandido = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.small,
                border =
                    BorderStroke(
                        1.dp,
                        if (error != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline,
                    ),
            ) {
                Text(
                    opciones.firstOrNull { it.first == valor }?.second ?: "Seleccionar",
                    Modifier.weight(1f),
                )
                Text("▾")
            }
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                if (opciones.isEmpty())
                    DropdownMenuItem(
                        text = { Text("No hay opciones disponibles") },
                        onClick = { expandido = false },
                        enabled = false,
                    )
                opciones.forEach { (id, texto) ->
                    DropdownMenuItem(
                        text = { Text(texto) },
                        onClick = {
                            cambio(id)
                            expandido = false
                        },
                    )
                }
            }
        }
        if (error != null)
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
    }
}
