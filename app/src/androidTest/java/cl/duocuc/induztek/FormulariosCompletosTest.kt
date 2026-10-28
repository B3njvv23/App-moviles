package cl.duocuc.induztek

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.ui.screens.FormularioScreen
import cl.duocuc.induztek.ui.theme.MiRegistroTheme
import cl.duocuc.induztek.viewmodel.*
import java.io.File
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** La actividad vacía muestra los mismos formularios con una base de prueba independiente. */
class FormulariosCompletosTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun todosLosFormulariosMuestranErroresPorCampoYPermitenCorregir() {
        val nombreDb = "qa-form-${id()}.db"
        val repo = EscenarioLocal(compose.activity, nombreDb)
        repo.login("mgodoy@induztek.ejemplo", "Induztek2026!")
        val visita =
            Visita(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                instalacionId = 1L,
                ot = "QA-FORM-${id()}",
                ingenieroId = 5L,
                participantes = listOf(1L),
                mantenimiento = true,
            )
        repo.visita(visita)
        val p =
            Protocolo(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                visitaId = visita.id,
                equipoId = 1L,
                creadorId = 1L,
                temperatura = 20.0,
                humedad = 50.0,
            )
        repo.protocolo(p)
        repo.login("respinoza@induztek.ejemplo", "Induztek2026!")
        val sesion = SesionViewModel(compose.activity.application, nombreDb)
        val vm =
            FormularioViewModel(sesion, cl.duocuc.induztek.viewmodel.ProtocolosViewModel(sesion))
        compose.setContent {
            MiRegistroTheme {
                val ui by vm.ui.collectAsState()
                FormularioScreen(ui.formulario, ui.ocupado, vm::valor, vm::guardar)
            }
        }
        compose.waitUntil(15000) { vm.ui.value.listo }

        data class Caso(
            val tipo: String,
            val ref: String = "",
            val extra: String = "",
            val invalidos: Map<String, String>,
            val corregidos: Map<String, String>,
        )
        val casos =
            listOf(
                Caso(
                    "cliente",
                    invalidos =
                        mapOf(
                            "nombre" to "",
                            "rut" to "123",
                            "giro" to "",
                            "contacto" to "",
                            "telefono" to "letras",
                            "correo" to "invalido",
                        ),
                    corregidos =
                        mapOf(
                            "nombre" to "Cliente QA",
                            "rut" to "17.284.519-3",
                            "giro" to "Industria",
                            "contacto" to "Contacto QA",
                            "telefono" to "+56900000000",
                            "correo" to "qa@cliente.ejemplo",
                        ),
                ),
                Caso(
                    "instalacion",
                    invalidos =
                        mapOf(
                            "cliente" to "",
                            "nombre" to "",
                            "ubicacion" to "",
                            "comuna" to "",
                            "region" to "",
                        ),
                    corregidos =
                        mapOf(
                            "cliente" to "1",
                            "nombre" to "Instalación QA",
                            "ubicacion" to "Ubicación QA",
                            "comuna" to "Lampa",
                            "region" to "Metropolitana",
                        ),
                ),
                Caso(
                    "equipo",
                    invalidos =
                        mapOf(
                            "tag" to "TR-01",
                            "marca" to "",
                            "serie" to "",
                            "anio" to "3000",
                            "placa" to "",
                            "max" to "NaN",
                        ),
                    corregidos =
                        mapOf(
                            "tag" to "QA-${id()}",
                            "marca" to "Marca QA",
                            "serie" to "SERIE-QA",
                            "anio" to "2020",
                            "placa" to "1500 kVA",
                            "max" to "0",
                        ),
                ),
                Caso(
                    "instrumento",
                    invalidos =
                        mapOf(
                            "nombre" to "",
                            "marca" to "",
                            "modelo" to "",
                            "serie" to "MIT-0045821",
                            "desde" to "2026-02-30",
                            "hasta" to "incorrecta",
                        ),
                    corregidos =
                        mapOf(
                            "nombre" to "Instrumento QA",
                            "marca" to "Marca",
                            "modelo" to "Modelo",
                            "serie" to "QA-${id()}",
                            "desde" to LocalDate.now().toString(),
                            "hasta" to LocalDate.now().plusYears(1).toString(),
                        ),
                ),
                Caso(
                    "visita",
                    invalidos =
                        mapOf(
                            "ot" to "OT-2026-1187",
                            "instalacion" to "",
                            "ingeniero" to "",
                            "fecha" to "no-fecha",
                        ),
                    corregidos =
                        mapOf(
                            "ot" to "QA-${id()}",
                            "instalacion" to "1",
                            "ingeniero" to "5",
                            "fecha" to LocalDate.now().toString(),
                        ),
                ),
                Caso(
                    "protocolo",
                    visita.id.toString(),
                    invalidos =
                        mapOf(
                            "equipo" to "",
                            "temperatura" to "81",
                            "humedad" to "101",
                            "mantenimiento" to "",
                            "reemplaza" to "PT-NO-EXISTE",
                        ),
                    corregidos =
                        mapOf(
                            "equipo" to "2",
                            "temperatura" to "20",
                            "humedad" to "50",
                            "mantenimiento" to "Limpieza QA",
                            "reemplaza" to "",
                        ),
                ),
                Caso(
                    "editar",
                    p.id.toString(),
                    invalidos =
                        mapOf("temperatura" to "NaN", "humedad" to "-1", "mantenimiento" to ""),
                    corregidos =
                        mapOf(
                            "temperatura" to "20",
                            "humedad" to "50",
                            "mantenimiento" to "Limpieza QA",
                        ),
                ),
                Caso(
                    "medicion",
                    p.id.toString(),
                    "AISLACION|AT-T",
                    invalidos =
                        mapOf(
                            "realizada" to "si",
                            "instrumento" to "",
                            "v" to "0",
                            "r1" to "-1",
                            "r10" to "NaN",
                        ),
                    corregidos =
                        mapOf("instrumento" to "1", "v" to "5", "r1" to "1000", "r10" to "2500"),
                ),
                Caso(
                    "cerrar",
                    visita.id.toString(),
                    invalidos = mapOf("nombre" to "", "rut" to "123", "cargo" to ""),
                    corregidos =
                        mapOf(
                            "nombre" to "Receptor QA",
                            "rut" to "17.284.519-3",
                            "cargo" to "Encargado",
                        ),
                ),
                Caso(
                    "estado",
                    "p2",
                    "OBSERVADO",
                    invalidos = mapOf("motivo" to ""),
                    corregidos = mapOf("motivo" to "Revisar instrumento"),
                ),
                Caso(
                    "estado",
                    p.id.toString(),
                    "ANULADO",
                    invalidos = mapOf("motivo" to ""),
                    corregidos = mapOf("motivo" to "Prueba cancelada"),
                ),
                Caso(
                    "usuario",
                    invalidos =
                        mapOf(
                            "nombre" to "",
                            "rut" to "17.284.519-3",
                            "correo" to "mgodoy@induztek.ejemplo",
                            "clave" to "corta",
                        ),
                    corregidos =
                        mapOf(
                            "nombre" to "Usuario QA",
                            "rut" to "11.111.111-1",
                            "correo" to "qa-${id()}@induztek.ejemplo",
                            "clave" to "ClaveDePrueba2026!",
                        ),
                ),
            )
        for (caso in casos) {
            compose.runOnIdle {
                vm.abrir(caso.tipo, caso.ref, caso.extra)
                caso.invalidos.forEach { (k, v) -> vm.valor(k, v) }
                vm.guardar()
            }
            compose.waitForIdle()
            val errores = vm.ui.value.formulario!!.errores
            assertTrue("${caso.tipo}: faltan validaciones", errores.isNotEmpty())
            for ((campo, error) in errores) {
                if (campo == "_general")
                    compose.onNodeWithTag("error_general").performScrollTo().assertTextEquals(error)
                else
                    compose
                        .onNodeWithTag("campo_$campo")
                        .performScrollTo()
                        .assert(hasAnyDescendant(hasText(error)))
            }
            val dir =
                File(compose.activity.getExternalFilesDir(null), "evidencias-qa").apply { mkdirs() }
            File(dir, "formulario-${caso.tipo}-${caso.extra.substringBefore('|')}.png")
                .outputStream()
                .use { out ->
                    compose
                        .onRoot()
                        .captureToImage()
                        .asAndroidBitmap()
                        .compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            compose.runOnIdle { caso.corregidos.forEach { (k, v) -> vm.valor(k, v) } }
            compose.waitForIdle()
            assertTrue(
                "${caso.tipo}: persisten errores tras corregir: ${vm.ui.value.formulario!!.errores}",
                vm.ui.value.formulario!!.errores.keys.all { it == "_general" },
            )
        }
        // Los errores de campos ocultos desaparecen cuando cambia el estado de una prueba.
        compose.runOnIdle {
            vm.abrir("medicion", p.id.toString(), "AISLACION|AT-T")
            vm.valor("realizada", "no")
            vm.guardar()
        }
        compose
            .onNodeWithTag("campo_motivo")
            .performScrollTo()
            .assert(hasAnyDescendant(hasText("Indica por qué no se realizó")))
        compose.runOnIdle { vm.valor("motivo", "Cliente no autorizó") }
        assertFalse(vm.ui.value.formulario!!.errores.containsKey("motivo"))
        compose.runOnIdle { vm.valor("realizada", "si") }
        compose.onNodeWithTag("campo_motivo").assertDoesNotExist()
        // Registro válido: se persiste y no solo desaparecen las etiquetas de error.
        val nuevoRut = "11.111.111-1"
        compose.runOnIdle {
            vm.abrir("cliente")
            mapOf(
                    "nombre" to "Cliente verificado QA",
                    "rut" to nuevoRut,
                    "giro" to "Industria",
                    "contacto" to "Contacto QA",
                    "telefono" to "+56900000000",
                    "correo" to "qa@cliente.ejemplo",
                )
                .forEach { (k, v) -> vm.valor(k, v) }
            vm.guardar()
        }
        compose.waitUntil(10000) { vm.ui.value.guardado > 0 }
        assertTrue(vm.ui.value.datos.clientes.any { it.rut == nuevoRut })
        assertTrue(
            EscenarioLocal(compose.activity, nombreDb).datos.value.clientes.any {
                it.rut == nuevoRut
            }
        )
    }
}
