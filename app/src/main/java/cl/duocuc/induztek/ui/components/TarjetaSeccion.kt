package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.ui.styles.EstilosTarjeta
import cl.duocuc.induztek.ui.theme.Dimens
import cl.duocuc.induztek.ui.theme.MiRegistroTheme

@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            contenido()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaSeccionPreview() {
    MiRegistroTheme {
        TarjetaSeccion(titulo = "Sobre mí", modifier = Modifier.padding(16.dp)) {
            Text("Cualquier contenido puede ir aquí dentro.")
        }
    }
}
