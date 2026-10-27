package cl.duocuc.induztek.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun VistaEvidencia(foto: ByteArray) {
    val bitmap =
        remember(foto) {
            runCatching {
                BitmapFactory.decodeByteArray(foto, 0, foto.size)
            }
                .getOrNull()
        }
    if (bitmap != null)
        Image(
            bitmap.asImageBitmap(),
            "Evidencia fotográfica del equipo",
            Modifier.fillMaxWidth().heightIn(max = 280.dp),
        )
    else Text("No se pudo mostrar la fotografía guardada.")
}
