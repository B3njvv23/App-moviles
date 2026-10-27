package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.ui.styles.EstilosTarjeta
import cl.duocuc.induztek.ui.theme.Dimens
import cl.duocuc.induztek.ui.theme.MiRegistroTheme

@Composable
fun TarjetaEstadistica(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = EstilosTarjeta.forma,
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaEstadisticaPreview() {
    MiRegistroTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaEstadistica("128", "Seguidores", Modifier.weight(1f))
            TarjetaEstadistica("96", "Siguiendo", Modifier.weight(1f))
        }
    }
}
