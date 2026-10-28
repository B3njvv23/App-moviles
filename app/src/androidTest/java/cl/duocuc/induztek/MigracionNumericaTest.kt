package cl.duocuc.induztek

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import java.io.File
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class MigracionNumericaTest {
    @Test
    fun convierteClavesRelacionadasFotoYPreferenciaDeSesion() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val nombre = "migracion-long-${UUID.randomUUID()}.db"
        val foto = byteArrayOf(1, 2, 3, 4)
        val archivoFoto = File(context.cacheDir, "foto-$nombre").apply { writeBytes(foto) }
        val datos = Semilla.crear()
        try {
            val archivo = context.getDatabasePath(nombre)
            archivo.parentFile!!.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(archivo, null).use { db ->
                val esquema =
                    instrumentation.context.assets.open("esquema_v2.sql").bufferedReader().use {
                        it.readText()
                    }
                esquema.split(';').filter { it.isNotBlank() }.forEach { db.execSQL(it) }
                Rol.entries.forEach { rol ->
                    db.insertOrThrow(
                        "roles",
                        null,
                        ContentValues().apply {
                            put("id", rol.name)
                            put("nombre", rol.texto)
                        },
                    )
                }
                val tablas =
                    linkedMapOf(
                        "clientes" to datos.clientes,
                        "usuarios" to datos.usuarios,
                        "instalaciones" to datos.instalaciones,
                        "equipos" to datos.equipos,
                        "instrumentos" to datos.instrumentos,
                        "visitas" to datos.visitas,
                        "protocolos" to datos.protocolos,
                    )
                db.beginTransaction()
                try {
                    tablas.forEach { (tabla, registros) ->
                        val columnas =
                            db.rawQuery("PRAGMA table_info($tabla)", null).use { c ->
                                buildList {
                                    while (c.moveToNext()) add(
                                        c.getString(c.getColumnIndexOrThrow("name"))
                                    )
                                }
                            }
                        registros.forEach { obj ->
                            val json = DatosVersion1Fixture.codificar(obj)
                            val valores = ContentValues()
                            columnas.forEach { columna ->
                                val valor = json.opt(columna)
                                when {
                                    columna == "evidencia" ->
                                        valores.put(columna, archivoFoto.absolutePath)
                                    valor is Boolean -> valores.put(columna, if (valor) 1 else 0)
                                    valor == null ||
                                        valor == org.json.JSONObject.NULL ||
                                        (columna.endsWith("Id") && valor == "") ->
                                        valores.putNull(columna)
                                    else -> valores.put(columna, valor.toString())
                                }
                            }
                            db.insertOrThrow(tabla, null, valores)
                        }
                    }
                    db.insertOrThrow(
                        "meta",
                        null,
                        ContentValues().apply {
                            put("clave", "recuperacion_u1")
                            put("valor", "hash-prueba")
                        },
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                db.version = 2
            }
            context
                .getSharedPreferences("sesion_$nombre", 0)
                .edit()
                .putString("usuario", "u1")
                .commit()
            InduztekDbHelper(context, nombre).use { db ->
                val restaurados = requireNotNull(db.cargar())
                assertEquals(
                    datos.usuarios.map { it.correo },
                    restaurados.usuarios.map { it.correo },
                )
                assertEquals(datos.equipos.map { it.tag }, restaurados.equipos.map { it.tag })
                val usuario = restaurados.usuarios.first()
                val protocolo = restaurados.protocolos.first()
                assertTrue(usuario.id > 0L)
                assertTrue(restaurados.visitas.any { it.id == protocolo.visitaId })
                assertArrayEquals(foto, protocolo.evidencia)
                assertEquals("hash-prueba", db.meta("recuperacion_${usuario.id}"))
                val preferencias = SesionPreferencias(context, "sesion_$nombre")
                preferencias.migrar(db)
                assertEquals(usuario.id, preferencias.leer())
                db.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use {
                    assertFalse(it.moveToFirst())
                }
                assertEquals(3, db.readableDatabase.version)
            }
        } finally {
            context.deleteDatabase(nombre)
            context.getSharedPreferences("sesion_$nombre", 0).edit().clear().commit()
            archivoFoto.delete()
        }
    }
}
