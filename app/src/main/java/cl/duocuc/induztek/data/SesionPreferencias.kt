package cl.duocuc.induztek.data

import android.content.Context
import androidx.core.content.edit

/** Conserva únicamente el identificador de la sesión en el dispositivo. */
class SesionPreferencias(context: Context, nombre: String = "sesion") {
    private val prefs = context.getSharedPreferences(nombre, Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "usuario"
    }

    fun guardar(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    fun leer(): Long = (prefs.all[CLAVE_ID_USUARIO] as? Long) ?: -1L

    fun migrar(db: InduztekDbHelper) {
        val anterior = prefs.all[CLAVE_ID_USUARIO] as? String ?: return
        val nuevo = db.meta("usuario_anterior_$anterior")?.toLongOrNull()
        if (nuevo != null) guardar(nuevo) else borrar()
    }

    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }
}
