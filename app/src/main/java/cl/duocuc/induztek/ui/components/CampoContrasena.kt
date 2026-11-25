package cl.duocuc.induztek.ui.components
// Aquí pido una contraseña y muestro su nivel de seguridad.

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.R
import cl.duocuc.induztek.ui.styles.EstilosCampo
import cl.duocuc.induztek.ui.theme.Dimens
import cl.duocuc.induztek.ui.theme.MiRegistroTheme
import cl.duocuc.induztek.viewmodel.Reglas

@Composable
fun CampoContrasena(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    visible: Boolean,
    onAlternarVisible: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    mostrarSeguridad: Boolean = true,
) {
    Column(modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(if (error != null) R.drawable.ic_error else R.drawable.ic_candado),
                contentDescription = if (error != null) "Campo con error" else null,
                modifier = Modifier.size(Dimens.tamanoIcono),
                tint = if (error != null) androidx.compose.material3.MaterialTheme.colorScheme.error
                    else androidx.compose.material3.LocalContentColor.current,
            )
        },
        trailingIcon = {
            IconButton(onClick = onAlternarVisible) {
                Icon(
                    painter =
                        painterResource(
                            if (visible) R.drawable.ic_ojo_tachado else R.drawable.ic_ojo
                        ),
                    contentDescription =
                        stringResource(
                            if (visible) R.string.cd_ocultar_contrasena
                            else R.string.cd_mostrar_contrasena
                        ),
                    modifier = Modifier.size(Dimens.tamanoIcono),
                )
            }
        },
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),
    )
    if (mostrarSeguridad && valor.isNotEmpty()) {
        val avance = listOf(valor.length >= 10, valor.any(Char::isUpperCase), valor.any(Char::isDigit), valor.any { !it.isLetterOrDigit() }).count { it }
        LinearProgressIndicator(progress = { avance / 4f }, modifier = Modifier.fillMaxWidth())
        Text(if (Reglas.claveSegura(valor)) "Contraseña segura" else "Requiere 10 caracteres, mayúscula, número y símbolo")
    }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoContrasenaPreview() {
    MiRegistroTheme {
        CampoContrasena(
            valor = "secreto",
            onValorCambia = {},
            etiqueta = "Contraseña",
            visible = false,
            onAlternarVisible = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
