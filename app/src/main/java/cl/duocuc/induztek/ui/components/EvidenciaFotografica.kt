package cl.duocuc.induztek.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import cl.duocuc.induztek.model.Protocolo
import cl.duocuc.induztek.viewmodel.ProtocolosViewModel

@Composable
fun EvidenciaFotografica(p: Protocolo, vm: ProtocolosViewModel) {
    val context = LocalContext.current
    var estado by rememberSaveable { mutableStateOf("") }
    var capturando by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(p.evidencia) {
        if (p.evidencia != null) estado = "Evidencia guardada en la base local"
    }
    val captura =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
            capturando = false
            if (foto != null) {
                estado = "Fotografía capturada; guardando evidencia…"
                vm.foto(p.id, foto)
            } else {
                estado = "Captura cancelada; se conserva la evidencia anterior"
                vm.mensaje(estado)
            }
        }
    fun tomar() {
        try {
            capturando = true
            estado = "Cámara abierta; confirma la fotografía o vuelve para cancelar"
            captura.launch(null)
        } catch (e: Exception) {
            capturando = false
            estado = "Cámara no disponible. Puedes continuar sin fotografía."
            vm.mensaje(estado)
        }
    }
    val permiso =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido
            ->
            if (concedido) tomar()
            else {
                estado = "Permiso de cámara denegado. Puedes continuar sin fotografía."
                vm.mensaje(estado)
            }
        }
    TarjetaSeccion("Evidencia del equipo") {
        Text(
            "Fotografía opcional para respaldar la identificación del equipo y las observaciones de terreno."
        )
        Button(
            onClick = {
                if (
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                )
                    tomar()
                else permiso.launch(Manifest.permission.CAMERA)
            },
            enabled = !capturando,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tomar fotografía")
        }
        if (estado.isNotBlank()) Text(estado, style = MaterialTheme.typography.bodyMedium)
        p.evidencia?.let { VistaEvidencia(it) }
    }
}
