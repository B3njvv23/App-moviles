package cl.duocuc.induztek.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.R
import cl.duocuc.induztek.ui.styles.EstilosCampo
import cl.duocuc.induztek.ui.theme.Dimens
import cl.duocuc.induztek.ui.theme.MiRegistroTheme

@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono),
            )
        },
        isError = error != null,
        trailingIcon =
            if (error != null) {
                {
                    Icon(
                        painterResource(R.drawable.ic_error),
                        "Campo con error",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                }
            } else null,
        supportingText = { if (error != null) Text(error) },
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = tipoTeclado,
                imeAction = ImeAction.Next,
            ),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoTextoPreview() {
    MiRegistroTheme {
        CampoTexto(
            valor = "ana@",
            onValorCambia = {},
            etiqueta = "Correo electrónico",
            icono = R.drawable.ic_correo,
            error = "Ingresa un correo válido",
            modifier = Modifier.padding(16.dp),
        )
    }
}
