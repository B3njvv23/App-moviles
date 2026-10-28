package cl.duocuc.induztek

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cl.duocuc.induztek.navigation.AppNavegacion
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.ui.theme.MiRegistroTheme
import cl.duocuc.induztek.viewmodel.*
import org.junit.Rule
import org.junit.Test

class AccesoNavegacionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun enlacesYErroresDeRegistroYRecuperacion() {
        val vm =
            SesionViewModel(
                compose.activity.application,
                "acceso-${java.util.UUID.randomUUID()}.db",
            )
        compose.setContent { MiRegistroTheme { AppNavegacion(vm) } }
        compose.waitUntil(15000) { vm.ui.value.listo }
        compose.onNodeWithText("Registrarte").performScrollTo().performClick()
        compose.onNodeWithText("Crea tu cuenta de cliente").assertExists()
        compose.onNodeWithText("Solicitar cuenta").performScrollTo().performClick()
        compose.waitUntil { vm.ui.value.erroresAcceso.size == 6 }
        compose.onNodeWithText("Volver a iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Olvidé mi contraseña").performScrollTo().performClick()
        compose.onNodeWithText("Recuperar contraseña").assertExists()
        compose.onNodeWithText("Restablecer contraseña").performScrollTo().performClick()
        compose.waitUntil { vm.ui.value.erroresAcceso.size == 4 }
        compose.onNodeWithText("Volver a iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Iniciar sesión").performScrollTo()
        java.io.File(compose.activity.filesDir, "login-v1.2.png").outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
