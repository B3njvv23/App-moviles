package cl.duocuc.induztek.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.native.Documentos
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExportarDocumento(
    d: Datos,
    visitaId: Long = 0L,
    protocoloId: Long = 0L,
    mensaje: (String) -> Unit,
    alGuardar: (android.net.Uri) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var archivo by rememberSaveable { mutableStateOf("") }
    var ocupado by remember { mutableStateOf(false) }
    var estadoDocumento by rememberSaveable { mutableStateOf("") }
    val guardar =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/pdf")
        ) { uri ->
            if (uri != null && archivo.isNotBlank())
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            Documentos.copiar(context, File(archivo), uri)
                        }
                        alGuardar(uri)
                        estadoDocumento = "PDF guardado correctamente en la ubicación elegida"
                        mensaje("PDF guardado")
                    } catch (e: Exception) {
                        estadoDocumento =
                            "No se pudo guardar el PDF. El acta y los datos de la visita se conservan; vuelve a intentarlo."
                        mensaje(estadoDocumento)
                    }
                }
            else {
                estadoDocumento =
                    "Guardado cancelado; el acta y los datos de la visita se conservan"
            }
        }
    fun generar(compartir: Boolean) {
        scope.launch {
            ocupado = true
            estadoDocumento = "Preparando documento…"
            try {
                val file =
                    withContext(Dispatchers.IO) {
                        Documentos.crear(context, Documentos.lineas(d, visitaId, protocoloId))
                    }
                archivo = file.absolutePath
                if (compartir) {
                    Documentos.compartir(context, file)
                    estadoDocumento =
                        "Selecciona una aplicación y confirma la entrega con el receptor"
                } else {
                    estadoDocumento = "Elige dónde guardar el PDF o cancela para volver"
                    guardar.launch(file.name)
                }
            } catch (e: Exception) {
                estadoDocumento = "No se pudo generar el documento; los datos se conservan"
                mensaje("$estadoDocumento: ${e.message}")
            } finally {
                ocupado = false
            }
        }
    }
    Button(onClick = { generar(false) }, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
        Text("Guardar PDF en el dispositivo")
    }
    OutlinedButton(
        onClick = { generar(true) },
        enabled = !ocupado,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Compartir PDF con el cliente")
    }
    if (estadoDocumento.isNotBlank())
        Text(estadoDocumento, style = MaterialTheme.typography.bodyMedium)
}
