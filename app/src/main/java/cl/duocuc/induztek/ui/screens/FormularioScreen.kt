package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun FormularioScreen(
    f: Formulario?,
    ocupado: Boolean,
    cambio: (String, String) -> Unit,
    guardar: () -> Unit,
) {
    if (f == null) {
        Contenido { Vacio("Vuelve a abrir el formulario.") }
        return
    }
    Contenido {
        Titulo(f.titulo)
        Text(
            "Los datos se guardarán localmente al confirmar.",
            style = MaterialTheme.typography.bodySmall,
        )
        f.errores["_general"]?.let {
            Text(
                it,
                Modifier.testTag("error_general"),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        TarjetaSeccion("Datos requeridos") {
            f.campos.forEach { c ->
                val oculta =
                    f.tipo == "medicion" &&
                        ((f.valores["realizada"] == "no" &&
                            (c.numero || c.clave in listOf("instrumento", "unidad"))) ||
                            (f.valores["realizada"] == "si" && c.clave == "motivo")) ||
                        (f.tipo == "usuario" &&
                            f.valores["rol"] != "CLIENTE" &&
                            c.clave == "cliente")
                if (!oculta) {
                    key(c.clave) {
                        Column(Modifier.testTag("campo_${c.clave}")) {
                            val valor = f.valores[c.clave].orEmpty()
                            val error = f.errores[c.clave]
                            if (c.selector)
                                Selector(
                                    c.etiqueta,
                                    valor,
                                    c.opciones,
                                    { cambio(c.clave, it) },
                                    error,
                                )
                            else if (c.clave == "clave") {
                                var visible by remember { mutableStateOf(false) }
                                CampoContrasena(
                                    valor = valor,
                                    onValorCambia = { cambio(c.clave, it) },
                                    etiqueta = c.etiqueta,
                                    visible = visible,
                                    onAlternarVisible = { visible = !visible },
                                    error = error,
                                )
                            } else
                                CampoTexto(
                                    valor,
                                    { cambio(c.clave, it) },
                                    c.etiqueta,
                                    when (c.clave) {
                                        "correo" -> R.drawable.ic_correo
                                        "nombre",
                                        "rut" -> R.drawable.ic_persona
                                        else -> R.drawable.ic_check
                                    },
                                    error = error,
                                    tipoTeclado =
                                        if (c.numero) KeyboardType.Decimal else KeyboardType.Text,
                                )
                        }
                    }
                }
            }
        }
        if (f.errores.isNotEmpty())
            Text(
                "Revisa los campos indicados antes de guardar.",
                color = MaterialTheme.colorScheme.error,
            )
        if (f.tipo == "medicion")
            Text(
                "Los resultados manuales los asigna el ingeniero. Las pruebas no realizadas quedan con su motivo y no cuentan en el resultado global.",
                style = MaterialTheme.typography.bodySmall,
            )
        BotonPrincipal(
            "Guardar",
            guardar,
            Modifier.fillMaxWidth(),
            habilitado = !ocupado,
            icono = R.drawable.ic_check,
        )
    }
}
