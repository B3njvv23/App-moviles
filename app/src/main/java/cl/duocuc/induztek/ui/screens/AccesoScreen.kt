package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.UiState
import cl.duocuc.induztek.ui.components.*

@Composable
fun AccesoScreen(
    registro: Boolean,
    ui: UiState,
    enviar: (Boolean, Map<String, String>) -> Unit,
    volver: () -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var empresa by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var codigo by rememberSaveable { mutableStateOf("") }
    // Las contraseñas no se guardan en el estado restaurable de Android.
    var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var visibleConfirmar by remember { mutableStateOf(false) }
    LaunchedEffect(ui.resultadoAcceso) {
        if (ui.resultadoAcceso.isNotBlank()) {
            clave = ""
            confirmar = ""
        }
    }
    Contenido {
        Etiqueta(stringResource(R.string.app_name))
        Titulo(stringResource(if (registro) R.string.register_title else R.string.recovery_title))
        Text(
            stringResource(
                if (registro) R.string.register_description else R.string.recovery_description
            )
        )
        if (ui.resultadoAcceso.isNotBlank()) {
            TarjetaSeccion(stringResource(R.string.auth_success)) {
                Text(
                    stringResource(
                        if (registro) R.string.register_success else R.string.recovery_success
                    )
                )
                if (registro) {
                    Text(stringResource(R.string.recovery_save_code))
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(ui.resultadoAcceso, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        } else
            TarjetaSeccion(
                stringResource(if (registro) R.string.register_data else R.string.recovery_data)
            ) {
                if (registro) {
                    CampoTexto(
                        nombre,
                        { nombre = it },
                        stringResource(R.string.field_name),
                        R.drawable.ic_persona,
                        error = ui.erroresAcceso["nombre"],
                    )
                    CampoTexto(
                        rut,
                        { rut = it },
                        stringResource(R.string.field_rut),
                        R.drawable.ic_persona,
                        error = ui.erroresAcceso["rut"],
                    )
                    CampoTexto(
                        empresa,
                        { empresa = it },
                        stringResource(R.string.field_company_rut),
                        R.drawable.ic_persona,
                        error = ui.erroresAcceso["empresa"],
                    )
                }
                CampoTexto(
                    correo,
                    { correo = it },
                    stringResource(R.string.field_email),
                    R.drawable.ic_correo,
                    error = ui.erroresAcceso["correo"],
                    tipoTeclado = KeyboardType.Email,
                )
                if (!registro)
                    CampoTexto(
                        codigo,
                        { codigo = it },
                        stringResource(R.string.field_recovery_code),
                        R.drawable.ic_persona,
                        error = ui.erroresAcceso["codigo"],
                    )
                CampoContrasena(
                    clave,
                    { clave = it },
                    stringResource(R.string.field_new_password),
                    visible,
                    { visible = !visible },
                    error = ui.erroresAcceso["clave"],
                )
                Text(
                    stringResource(R.string.password_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                CampoContrasena(
                    confirmar,
                    { confirmar = it },
                    stringResource(R.string.field_confirm_password),
                    visibleConfirmar,
                    { visibleConfirmar = !visibleConfirmar },
                    error = ui.erroresAcceso["confirmar"],
                )
                ui.erroresAcceso["_general"]?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                BotonPrincipal(
                    stringResource(
                        if (registro) R.string.register_submit else R.string.recovery_submit
                    ),
                    {
                        enviar(
                            registro,
                            mapOf(
                                "nombre" to nombre,
                                "rut" to rut,
                                "empresa" to empresa,
                                "correo" to correo,
                                "codigo" to codigo,
                                "clave" to clave,
                                "confirmar" to confirmar,
                            ),
                        )
                    },
                    Modifier.fillMaxWidth(),
                    habilitado = !ui.ocupado,
                )
            }
        TextButton(onClick = volver, enabled = !ui.ocupado) {
            Text(stringResource(R.string.back_login))
        }
    }
}
