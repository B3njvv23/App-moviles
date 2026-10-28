package cl.duocuc.induztek

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.induztek.data.InduztekDbHelper
import cl.duocuc.induztek.data.Semilla
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class SqliteRelacionalTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun columnasRelacionadasYRollbackAnteRelacionInvalida() {
        val nombre = "relacional-${UUID.randomUUID()}.db"
        val datos = Semilla.crear()
        try {
            InduztekDbHelper(context, nombre).use { helper ->
                helper.precargar(datos)
                helper.readableDatabase.rawQuery("PRAGMA table_info(equipos)", null).use { c ->
                    val columnas = buildList {
                        while (c.moveToNext()) add(c.getString(c.getColumnIndexOrThrow("name")))
                    }
                    assertTrue(columnas.containsAll(listOf("tag", "marca", "instalacionId")))
                    assertFalse(columnas.contains("json"))
                }
                helper.readableDatabase
                    .rawQuery(
                        "SELECT COUNT(*) FROM usuarios u INNER JOIN roles r ON u.rol=r.id",
                        null,
                    )
                    .use { c ->
                        assertTrue(c.moveToFirst())
                        assertEquals(datos.usuarios.size, c.getInt(0))
                    }
                assertThrows(android.database.sqlite.SQLiteConstraintException::class.java) {
                    helper.actualizarProtocolo(datos.protocolos.first().copy(equipoId = 999999L))
                }
                assertEquals(datos.equipos, helper.cargar()!!.equipos)
            }
        } finally {
            context.deleteDatabase(nombre)
        }
    }

    @Test
    fun migraVersionAnteriorSinPerderDatosNiSesionDeRecuperacion() {
        val nombre = "migracion-${UUID.randomUUID()}.db"
        val datos = Semilla.crear()
        try {
            val archivo = context.getDatabasePath(nombre)
            archivo.parentFile!!.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(archivo, null).use { db ->
                val tablas =
                    linkedMapOf(
                        "usuarios" to datos.usuarios,
                        "clientes" to datos.clientes,
                        "instalaciones" to datos.instalaciones,
                        "equipos" to datos.equipos,
                        "instrumentos" to datos.instrumentos,
                        "visitas" to datos.visitas,
                        "protocolos" to datos.protocolos,
                    )
                tablas.forEach { (tabla, registros) ->
                    db.execSQL(
                        "CREATE TABLE $tabla(id TEXT PRIMARY KEY,json TEXT NOT NULL,clave TEXT,ambito TEXT)"
                    )
                    registros.forEach { obj ->
                        val json = DatosVersion1Fixture.codificar(obj)
                        db.insertOrThrow(
                            tabla,
                            null,
                            ContentValues().apply {
                                put("id", json.getString("id"))
                                put("json", json.toString())
                            },
                        )
                    }
                }
                db.execSQL("CREATE TABLE meta(clave TEXT PRIMARY KEY,valor TEXT NOT NULL)")
                db.insertOrThrow(
                    "meta",
                    null,
                    ContentValues().apply {
                        put("clave", "recuperacion_u1")
                        put("valor", "hash-de-prueba")
                    },
                )
                db.version = 1
            }
            InduztekDbHelper(context, nombre).use { helper ->
                val restaurados = requireNotNull(helper.cargar())
                assertEquals(
                    datos.usuarios.map { it.correo },
                    restaurados.usuarios.map { it.correo },
                )
                assertEquals(datos.equipos.map { it.tag }, restaurados.equipos.map { it.tag })
                assertEquals(
                    datos.protocolos.map { it.mediciones.size },
                    restaurados.protocolos.map { it.mediciones.size },
                )
                val usuario =
                    restaurados.usuarios.first { it.correo == datos.usuarios.first().correo }
                assertEquals("hash-de-prueba", helper.meta("recuperacion_${usuario.id}"))
                assertEquals(3, helper.readableDatabase.version)
                helper.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use {
                    assertFalse(it.moveToFirst())
                }
            }
        } finally {
            context.deleteDatabase(nombre)
        }
    }
}
