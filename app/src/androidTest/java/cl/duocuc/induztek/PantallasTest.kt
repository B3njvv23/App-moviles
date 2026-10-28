package cl.duocuc.induztek

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Rule
import org.junit.Test

class PantallasTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun ingresoNavegacionHistorialYValidacionVisual() {
        compose.waitUntil(15000) {
            compose.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty()
        }
        if (compose.onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("Salir").performClick()
        compose.waitUntil(15000) {
            compose.onAllNodesWithText("Técnico · mgodoy").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Técnico · mgodoy").performScrollTo().performClick()
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        compose.waitUntil(15000) {
            compose.onAllNodesWithText("Tu jornada en terreno").fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitUntil(8000) {
            compose.onAllNodesWithText("Sesión iniciada").fetchSemanticsNodes().isEmpty()
        }
        captura("inicio.png")
        compose.onNode(hasText("Equipos") and hasClickAction()).performClick()
        compose.onNodeWithText("TR-01").performScrollTo().performClick()
        compose.onNodeWithText("Historial").assertExists()
        captura("historial.png")
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Registrar equipo").performScrollTo().performClick()
        compose.onNodeWithText("Guardar").performScrollTo().performClick()
        captura("validacion-estado.png")
        compose.waitUntil(5000) {
            compose
                .onAllNodesWithText("Revisa los campos indicados antes de guardar.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Guardar").performScrollTo()
        captura("validacion.png")
        compose.onNodeWithText("Revisa los campos indicados antes de guardar.").assertExists()
        compose.onNodeWithText("Salir").performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Técnico · mgodoy").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun captura(nombre: String) {
        val file = File(compose.activity.filesDir, nombre)
        file.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
