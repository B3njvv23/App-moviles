package cl.duocuc.induztek

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.waitUntil
import cl.duocuc.induztek.navigation.AppNavegacion
import cl.duocuc.induztek.ui.theme.MiRegistroTheme
import cl.duocuc.induztek.viewmodel.SesionViewModel
import org.junit.Rule
import org.junit.Test

class AccesoNavegacionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun permiteRecorrerAccesoRegistroYRecuperacionConErroresVisibles() {
        val nombreDb = "acceso-${java.util.UUID.randomUUID()}.db"
        val vm = SesionViewModel(compose.activity.application, nombreDb)
        compose.setContent { MiRegistroTheme { AppNavegacion(vm) } }
        compose.waitUntil(15000) { vm.ui.value.listo }

        compose.onNodeWithText("Registrarte").performScrollTo().performClick()
        compose.onNodeWithText("Crea tu cuenta de cliente").assertExists()
        compose.onNodeWithText("Solicitar cuenta").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.ui.value.erroresAcceso.size == 6 }

        compose.onNodeWithText("Volver a iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Olvidé mi contraseña").performScrollTo().performClick()
        compose.onNodeWithText("Recuperar contraseña").assertExists()
        compose.onNodeWithText("Restablecer contraseña").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.ui.value.erroresAcceso.size == 4 }
        compose.onNodeWithText("Volver a iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Iniciar sesión").performScrollTo().assertExists()
    }
}
