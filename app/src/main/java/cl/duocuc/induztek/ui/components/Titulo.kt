package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun Titulo(t: String, detalle: String = "") {
    Column {
        Text(t, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (detalle.isNotBlank())
            Text(
                detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}
