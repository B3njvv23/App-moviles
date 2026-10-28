package cl.duocuc.induztek

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.native.Documentos
import cl.duocuc.induztek.viewmodel.Reglas
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoTest {
    @Test
    fun visitaSinConexionPersistenciaActaYPermisos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = EscenarioLocal(context)
        repo.login("mgodoy@induztek.ejemplo", "Induztek2026!")
        val visita =
            Visita(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                instalacionId = 1L,
                ot = "OT-TEST-${id()}",
                ingenieroId = 5L,
                participantes = listOf(1L),
            )
        repo.visita(visita)
        val p =
            Protocolo(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                visitaId = visita.id,
                equipoId = 2L,
                creadorId = 1L,
                temperatura = 19.0,
                humedad = 60.0,
            )
        repo.protocolo(p)
        val restaurado = EscenarioLocal(context)
        assertTrue(restaurado.visible().protocolos.any { it.id == p.id })
        for (pr in Reglas.pruebas(Tipo.TRANSFORMADOR)) for (elemento in
            Reglas.elementos(Tipo.TRANSFORMADOR, pr)) {
            if (pr == Prueba.AISLACION && elemento == "AT-T")
                repo.medicion(
                    p.id,
                    Medicion(
                        id = cl.duocuc.induztek.nuevoIdPrueba(),
                        prueba = pr,
                        elemento = elemento,
                        instrumentoId = 1L,
                        valores = mapOf("v" to 5.0, "r1" to 6100.0, "r10" to 10980.0),
                    ),
                )
            else
                repo.medicion(
                    p.id,
                    Medicion(
                        id = cl.duocuc.induztek.nuevoIdPrueba(),
                        prueba = pr,
                        elemento = elemento,
                        realizada = false,
                        motivo = "Ventana de corte finalizada",
                    ),
                )
        }
        repo.transicion(p.id, Estado.REGISTRADO, "Terminado")
        var denegado = false
        try {
            repo.transicion(p.id, Estado.FORMALIZADO, "Intento técnico")
        } catch (e: IllegalArgumentException) {
            denegado = true
        }
        assertTrue(denegado)
        repo.cerrar(
            visita.id,
            Recepcion("Marcela Fuentes Rojas", "14.226.791-8", "Jefa de mantenimiento"),
        )
        val lineas = Documentos.lineas(repo.visible(), visitaId = visita.id)
        assertTrue(lineas.any { it == "PENDIENTE DE FORMALIZACIÓN" })
        assertTrue(lineas.any { it.contains("NO REALIZADA") })
        val pdf = Documentos.crear(context, lineas)
        assertTrue(pdf.length() > 1000)
        pdf.copyTo(
            java.io.File(context.getExternalFilesDir(null), "acta-prueba.pdf"),
            overwrite = true,
        )
        repo.login("acontreras@induztek.ejemplo", "Induztek2026!")
        repo.transicion(p.id, Estado.FORMALIZADO, "Revisado en prueba")
        assertEquals(lineas, Documentos.lineas(repo.visible(), visitaId = visita.id))
        repo.login("mfuentes@envpacifico.ejemplo", "Induztek2026!")
        assertTrue(repo.visible().equipos.all { it.instalacionId == 1L })
        assertTrue(
            repo.visible().protocolos.all { it.estado !in listOf(Estado.BORRADOR, Estado.ANULADO) }
        )
        denegado = false
        try {
            repo.visita(visita.copy(id = cl.duocuc.induztek.nuevoIdPrueba(), ot = "NO-PERMITIDA"))
        } catch (e: IllegalArgumentException) {
            denegado = true
        }
        assertTrue(denegado)
        repo.salir()
    }

    @Test
    fun instrumentoVencidoYTagDuplicadoSeRechazan() {
        val repo = EscenarioLocal(ApplicationProvider.getApplicationContext())
        repo.login("mgodoy@induztek.ejemplo", "Induztek2026!")
        var denegado = false
        try {
            repo.equipo(
                repo
                    .visible()
                    .equipos
                    .first { it.id == 1L }
                    .copy(id = cl.duocuc.induztek.nuevoIdPrueba())
            )
        } catch (e: IllegalArgumentException) {
            denegado = true
        }
        assertTrue(denegado)
        val v =
            Visita(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                instalacionId = 1L,
                ot = "OT-CAL-${id()}",
                ingenieroId = 5L,
                participantes = listOf(1L),
            )
        repo.visita(v)
        val p =
            Protocolo(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                visitaId = v.id,
                equipoId = 1L,
                creadorId = 1L,
                temperatura = 20.0,
                humedad = 50.0,
            )
        repo.protocolo(p)
        denegado = false
        try {
            repo.medicion(
                p.id,
                Medicion(
                    id = cl.duocuc.induztek.nuevoIdPrueba(),
                    prueba = Prueba.AISLACION,
                    elemento = "AT-T",
                    instrumentoId = 2L,
                    valores = mapOf("v" to 5.0, "r1" to 1000.0, "r10" to 2000.0),
                ),
            )
        } catch (e: IllegalArgumentException) {
            denegado = true
        }
        assertTrue(denegado)
        repo.salir()
    }
}
