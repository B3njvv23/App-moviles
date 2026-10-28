package cl.duocuc.induztek

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.ui.screens.LoginScreen
import cl.duocuc.induztek.ui.theme.MiRegistroTheme
import cl.duocuc.induztek.viewmodel.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LoginValidacionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun camposVaciosCorreoIncorrectoClaveIncorrectaYAccesoCorrecto() {
        val sesion = SesionViewModel(compose.activity.application, "qa-login-${id()}.db")
        val vm = AccesoViewModel(sesion)
        compose.setContent {
            MiRegistroTheme {
                val ui by vm.ui.collectAsState()
                LoginScreen(ui.ocupado, vm::login, ui.erroresLogin, vm::limpiarLogin)
            }
        }
        compose.waitUntil(15000) { vm.ui.value.listo }
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        assertTrue(vm.ui.value.erroresLogin.containsKey("correo"))
        assertTrue(vm.ui.value.erroresLogin.containsKey("clave"))
        compose.onNodeWithText("Ingresa un correo válido").assertExists()
        compose.onNode(hasSetTextAction() and hasText("Correo")).performTextInput("sin-arroba")
        compose
            .onNode(hasSetTextAction() and hasText("Contraseña"))
            .performTextInput("ClaveIncorrecta")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        compose.onNodeWithText("Ingresa un correo válido").assertExists()
        compose
            .onNode(hasSetTextAction() and hasText("Correo"))
            .performTextReplacement("mgodoy@induztek.ejemplo")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.ui.value.erroresLogin.containsKey("clave") }
        compose.onNodeWithText("Correo o contraseña incorrectos; revisa ambos datos").assertExists()
        compose
            .onNode(hasSetTextAction() and hasText("Contraseña"))
            .performTextReplacement("Induztek2026!")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.ui.value.usuario != null }
        assertTrue(vm.ui.value.erroresLogin.isEmpty())
    }
}

