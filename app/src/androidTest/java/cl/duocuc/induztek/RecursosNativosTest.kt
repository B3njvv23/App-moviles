package cl.duocuc.induztek

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.ui.screens.*
import cl.duocuc.induztek.ui.theme.MiRegistroTheme
import cl.duocuc.induztek.viewmodel.*
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba real de permisos, Camera2 y DocumentsUI en la variante independiente .qa. */
@RunWith(AndroidJUnit4::class)
class RecursosNativosTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val device
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun res(id: String) = By.res(Pattern.compile(".*:id/$id"))

    private fun esperarTexto(texto: String) {
        compose.waitUntil(12000) {
            compose.onAllNodesWithText(texto, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun captura(nombre: String) {
        val dir =
            File(compose.activity.getExternalFilesDir(null), "evidencias-qa").apply { mkdirs() }
        File(dir, nombre).outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun dump(nombre: String) {
        device.dumpWindowHierarchy(File(compose.activity.getExternalFilesDir(null), nombre))
    }

    @Test
    fun camaraPermisoDenegadoCancelacionCapturaYReemplazoCancelado() {
        assumeTrue(
            "Ejecutar con -PverificacionNativa para proteger los datos de la app normal",
            compose.activity.packageName.endsWith(".qa"),
        )
        val db = "qa-camara-${id()}.db"
        val repo = EscenarioLocal(compose.activity, db)
        repo.login("mgodoy@induztek.ejemplo", "Induztek2026!")
        val v =
            Visita(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                instalacionId = 6L,
                ot = "QA-CAM-${id()}",
                ingenieroId = 5L,
                participantes = listOf(1L),
            )
        repo.visita(v)
        val p =
            Protocolo(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                visitaId = v.id,
                equipoId = 7L,
                creadorId = 1L,
                temperatura = 20.0,
                humedad = 50.0,
            )
        repo.protocolo(p)
        val original = repo.datos.value.protocolos.first { it.id == p.id }
        val vm = SesionViewModel(compose.activity.application, db)
        val protocolos = ProtocolosViewModel(vm)
        compose.setContent {
            MiRegistroTheme {
                val ui by vm.ui.collectAsState()
                ui.datos.protocolos
                    .firstOrNull { it.id == p.id }
                    ?.let { Contenido { EvidenciaFotografica(it, protocolos) } }
            }
        }
        compose.waitUntil(15000) { vm.ui.value.listo }
        compose.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        assertTrue(
            "Debe aparecer el permiso; revocar CAMERA de la app .qa antes de esta prueba",
            device.wait(Until.hasObject(res("permission_deny_button")), 10000),
        )
        dump("permiso-camara.xml")
        device.findObject(res("permission_deny_button")).click()
        esperarTexto("Permiso de cámara denegado")
        assertNull(vm.ui.value.datos.protocolos.first { it.id == p.id }.evidencia)
        captura("camara-denegada.png")

        compose.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        assertTrue(
            device.wait(Until.hasObject(res("permission_allow_foreground_only_button")), 10000)
        )
        device.findObject(res("permission_allow_foreground_only_button")).click()
        assertTrue(
            "Debe abrirse la cámara del dispositivo",
            device.wait(Until.hasObject(res("shutter_button")), 15000),
        )
        dump("camara-abierta.xml")
        device.pressBack()
        esperarTexto("Captura cancelada")
        assertEquals(original, vm.ui.value.datos.protocolos.first { it.id == p.id })
        captura("camara-cancelada.png")

        compose.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        assertTrue(device.wait(Until.hasObject(res("shutter_button")), 10000))
        device.findObject(res("shutter_button")).click()
        device.waitForIdle()
        dump("camara-confirmacion.xml")
        val confirmado =
            device.wait(Until.findObject(res("done_button")), 8000)
                ?: device.wait(Until.findObject(res("review_done")), 2000)
                ?: device.wait(
                    Until.findObject(
                        By.desc(Pattern.compile("(?i)(done|listo|aceptar|confirmar)"))
                    ),
                    2000,
                )
        assertNotNull("Debe existir el control para confirmar la fotografía", confirmado)
        confirmado!!.click()
        compose.waitUntil(15000) {
            vm.ui.value.datos.protocolos.first { it.id == p.id }.evidencia != null
        }
        val foto = requireNotNull(vm.ui.value.datos.protocolos.first { it.id == p.id }.evidencia)
        assertTrue(foto.isNotEmpty())
        assertNotNull(BitmapFactory.decodeByteArray(foto, 0, foto.size))
        assertArrayEquals(
            foto,
            EscenarioLocal(compose.activity, db)
                .datos
                .value
                .protocolos
                .first { it.id == p.id }
                .evidencia,
        )
        esperarTexto("Evidencia guardada")
        captura("camara-guardada.png")
        compose.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        assertTrue(device.wait(Until.hasObject(res("shutter_button")), 10000))
        device.pressBack()
        esperarTexto("Captura cancelada")
        assertArrayEquals(foto, vm.ui.value.datos.protocolos.first { it.id == p.id }.evidencia)

        captura("camara-reemplazo-cancelado.png")
    }

    @Test
    fun documentosCancelacionYGuardadoPdfRealSinPermisoGeneralDeArchivos() {
        assumeTrue(
            "Prueba de recursos nativos en la variante .qa",
            compose.activity.packageName.endsWith(".qa"),
        )
        val repo = EscenarioLocal(compose.activity, "qa-pdf-${id()}.db")
        repo.login("mgodoy@induztek.ejemplo", "Induztek2026!")
        val d = repo.visible()
        val uriGuardado = AtomicReference<Uri?>()
        compose.setContent {
            MiRegistroTheme {
                Contenido {
                    ExportarDocumento(
                        d,
                        visitaId = 3L,
                        mensaje = {},
                        alGuardar = { uriGuardado.set(it) },
                    )
                }
            }
        }
        compose.onNodeWithText("Guardar PDF en el dispositivo").performClick()
        assertTrue(device.wait(Until.hasObject(By.pkg(Pattern.compile(".*documentsui.*"))), 12000))
        dump("selector-pdf.xml")
        device.pressBack()
        esperarTexto("Guardado cancelado")
        assertNull(uriGuardado.get())
        assertEquals(d, repo.visible())
        captura("pdf-cancelado.png")

        compose.onNodeWithText("Guardar PDF en el dispositivo").performScrollTo().performClick()
        assertTrue(device.wait(Until.hasObject(By.pkg(Pattern.compile(".*documentsui.*"))), 10000))
        val nombre = "QA-Induztek-${id()}.pdf"
        val campo = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000)
        assertNotNull("Campo de nombre del PDF en el selector", campo)
        campo!!.text = nombre
        val guardar =
            device.wait(Until.findObject(By.text(Pattern.compile("(?i)(guardar|save)"))), 5000)
        assertNotNull("Botón Guardar del selector de Android", guardar)
        guardar!!.click()
        compose.waitUntil(15000) { uriGuardado.get() != null }
        esperarTexto("PDF guardado correctamente")
        val uri = uriGuardado.get()!!
        val bytes = compose.activity.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        assertTrue(bytes.size > 1000)
        assertEquals("%PDF", String(bytes.take(4).toByteArray()))
        compose.activity.contentResolver.openFileDescriptor(uri, "r")!!.use { fd ->
            PdfRenderer(fd).use { pdf -> assertTrue(pdf.pageCount >= 1) }
        }
        assertEquals(d, repo.visible())
        val dir =
            File(compose.activity.getExternalFilesDir(null), "evidencias-qa").apply { mkdirs() }
        File(dir, "acta-guardada.pdf").writeBytes(bytes)
        File(dir, "ubicacion-pdf.txt")
            .writeText(
                "Archivo de prueba: $nombre\nURI devuelta por Android: $uri\nTamaño: ${bytes.size} bytes\n"
            )
        captura("pdf-guardado.png")
    }
}
