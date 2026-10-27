package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.ui.styles.EstilosBoton
import cl.duocuc.induztek.ui.styles.estiloAltoBoton
import cl.duocuc.induztek.ui.theme.MiRegistroTheme

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.estiloAltoBoton(),
        shape = EstilosBoton.forma,
        colors = EstilosBoton.coloresSecundario(),
        border = EstilosBoton.bordeSecundario(),
        contentPadding = EstilosBoton.relleno,
    ) {
        Text(text = texto)
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonSecundarioPreview() {
    MiRegistroTheme {
        BotonSecundario(texto = "Limpiar", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
