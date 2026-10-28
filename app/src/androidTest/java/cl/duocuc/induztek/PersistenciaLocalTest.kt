package cl.duocuc.induztek

import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class PersistenciaLocalTest {
    @Test
    fun idsAutoincrementalesFotoBlobYActualizacionPorRegistro() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nombre = "long-blob-${UUID.randomUUID()}.db"
        var protocoloId = 0L
        val foto = byteArrayOf(1, 2, 3, 4, 5)
        try {
            InduztekDbHelper(context, nombre).use { db ->
                db.precargar(Semilla.crear())
                val visita =
                    Visita(
                        instalacionId = 1L,
                        ot = "OT-PRUEBA",
                        ingenieroId = 5L,
                        participantes = listOf(1L),
                    )
                val visitaId = db.insertarVisita(visita)
                assertTrue(visitaId > 0L)
                assertEquals(0L, visita.id)
                protocoloId =
                    db.insertarProtocolo(
                        Protocolo(
                            visitaId = visitaId,
                            equipoId = 1L,
                            creadorId = 1L,
                            temperatura = 20.0,
                            humedad = 50.0,
                        )
                    )
                val clientes = db.listarClientes()
                db.writableDatabase.execSQL("CREATE TABLE auditoria (cambios INTEGER)")
                db.writableDatabase.execSQL(
                    "CREATE TRIGGER vigilar_clientes AFTER DELETE ON clientes BEGIN INSERT INTO auditoria VALUES(1); END"
                )
                val protocolo = db.listarProtocolos().first { it.id == protocoloId }
                db.actualizarProtocolo(
                    protocolo.copy(evidencia = foto, observaciones = "Prueba BLOB")
                )
                assertEquals(clientes, db.listarClientes())
                db.readableDatabase.rawQuery("SELECT COUNT(*) FROM auditoria", null).use { c ->
                    c.moveToFirst()
                    assertEquals(0, c.getInt(0))
                }
                db.readableDatabase
                    .rawQuery(
                        "SELECT typeof(id),typeof(evidencia) FROM protocolos WHERE id=?",
                        arrayOf(protocoloId.toString()),
                    )
                    .use { c ->
                        c.moveToFirst()
                        assertEquals("integer", c.getString(0))
                        assertEquals("blob", c.getString(1))
                    }
                val segundoId = db.insertarVisita(visita.copy(ot = "OT-PRUEBA-2"))
                assertTrue(segundoId > visitaId)
            }
            InduztekDbHelper(context, nombre).use { db ->
                val p = db.listarProtocolos().first { it.id == protocoloId }
                assertArrayEquals(foto, p.evidencia)
                assertEquals("Prueba BLOB", p.observaciones)
            }
        } finally {
            context.deleteDatabase(nombre)
        }
    }
}
