package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun LoginScreen(
    ocupado: Boolean,
    login: (String, String) -> Unit,
    errores: Map<String, String> = emptyMap(),
    limpiar: () -> Unit = {},
    registrar: () -> Unit = {},
    recuperar: () -> Unit = {},
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    Contenido {
        Spacer(Modifier.height(32.dp))
        Etiqueta(stringResource(R.string.auth_banner))
        Titulo(stringResource(R.string.auth_brand), stringResource(R.string.auth_subtitle))
        TarjetaSeccion(stringResource(R.string.login_title)) {
            CampoTexto(
                correo,
                {
                    correo = it
                    limpiar()
                },
                stringResource(R.string.field_email),
                R.drawable.ic_correo,
                error = errores["correo"],
                tipoTeclado = KeyboardType.Email,
            )
            CampoContrasena(
                valor = clave,
                onValorCambia = {
                    clave = it
                    limpiar()
                },
                etiqueta = stringResource(R.string.field_password),
                visible = visible,
                onAlternarVisible = { visible = !visible },
                error = errores["clave"],
            )
            TextButton(onClick = recuperar, enabled = !ocupado) {
                Text(stringResource(R.string.forgot_password))
            }
            BotonPrincipal(
                stringResource(R.string.login_action),
                { login(correo, clave) },
                Modifier.fillMaxWidth(),
                habilitado = !ocupado,
                icono = R.drawable.ic_seguir,
            )
            Text(stringResource(R.string.no_account))
            BotonSecundario(
                stringResource(R.string.register_action),
                registrar,
                Modifier.fillMaxWidth(),
                habilitado = !ocupado,
            )
        }
        Text(stringResource(R.string.auth_offline), style = MaterialTheme.typography.bodySmall)
    }
}
