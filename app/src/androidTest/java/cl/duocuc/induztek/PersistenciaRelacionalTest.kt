package cl.duocuc.induztek

import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.induztek.data.InduztekDbHelper
import cl.duocuc.induztek.model.*
import java.util.UUID
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenciaRelacionalTest {
    @Test
    fun conservaRelacionesIdsYFotoBlobAlReabrirLaBase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nombreDb = "persistencia-${UUID.randomUUID()}.db"
        val foto = byteArrayOf(2, 4, 8, 16)
        var clienteId = 0L
        var usuarioId = 0L
        var instalacionId = 0L
        var equipoId = 0L
        var visitaId = 0L
        var protocoloId = 0L
        try {
            InduztekDbHelper(context, nombreDb).use { db ->
                clienteId = db.insertarCliente(
                    Cliente(
                        nombre = "Cliente de prueba",
                        rut = "RUT-PRUEBA-01",
                        giro = "Giro de prueba",
                        contacto = "Contacto de prueba",
                        telefono = "000000000",
                        correo = "cliente@example.test",
                    )
                )
                usuarioId = db.insertarUsuario(
                    Usuario(
                        nombre = "Usuario de prueba",
                        rut = "RUT-PRUEBA-02",
                        correo = "usuario@example.test",
                        rol = Rol.TECNICO,
                    )
                )
                instalacionId = db.insertarInstalacion(
                    Instalacion(
                        clienteId = clienteId,
                        nombre = "Instalación de prueba",
                        ubicacion = "Sector de prueba",
                        comuna = "Comuna de prueba",
                        region = "Región de prueba",
                        acceso = "",
                    )
                )
                equipoId = db.insertarEquipo(
                    Equipo(
                        instalacionId = instalacionId,
                        tag = "EQ-PRUEBA-01",
                        tipo = Tipo.TRANSFORMADOR,
                        marca = "Marca de prueba",
                        modelo = "Modelo de prueba",
                        serie = "SERIE-PRUEBA-01",
                        anio = 2025,
                        placa = "1500 kVA",
                    )
                )
                visitaId = db.insertarVisita(
                    Visita(
                        instalacionId = instalacionId,
                        ot = "OT-PRUEBA-01",
                        ingenieroId = usuarioId,
                        participantes = listOf(usuarioId),
                    )
                )
                protocoloId = db.insertarProtocolo(
                    Protocolo(
                        visitaId = visitaId,
                        equipoId = equipoId,
                        creadorId = usuarioId,
                        temperatura = 20.0,
                        humedad = 50.0,
                        evidencia = foto,
                    )
                )
                assertTrue(clienteId > 0L && usuarioId > 0L && protocoloId > 0L)
            }

            InduztekDbHelper(context, nombreDb).use { db ->
                val cliente = db.listarClientes().single { it.id == clienteId }
                val instalacion = db.listarInstalaciones().single { it.id == instalacionId }
                val equipo = db.listarEquipos().single { it.id == equipoId }
                val visita = db.listarVisitas().single { it.id == visitaId }
                val protocolo = db.listarProtocolos().single { it.id == protocoloId }

                assertEquals(cliente.id, instalacion.clienteId)
                assertEquals(instalacion.id, equipo.instalacionId)
                assertEquals(instalacion.id, visita.instalacionId)
                assertEquals(visita.id, protocolo.visitaId)
                assertEquals(equipo.id, protocolo.equipoId)
                assertEquals(listOf(usuarioId), visita.participantes)
                assertArrayEquals(foto, protocolo.evidencia)
                db.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use {
                    assertFalse(it.moveToFirst())
                }
            }
        } finally {
            context.deleteDatabase(nombreDb)
        }
    }
}
