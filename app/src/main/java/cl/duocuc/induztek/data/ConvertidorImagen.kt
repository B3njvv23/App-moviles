package cl.duocuc.induztek.data

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ConvertidorImagen {
    fun aBytes(imagen: Bitmap): ByteArray =
        ByteArrayOutputStream().use { salida ->
            check(imagen.compress(Bitmap.CompressFormat.JPEG, 85, salida)) {
                "No se pudo convertir la fotografía"
            }
            salida.toByteArray()
        }
}
