package cl.duocuc.induztek

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.induztek.model.Rol
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistroRecuperacionTest {
    @Test
    fun registroPendienteAprobacionYRecuperacionPersisten() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val nombreDb = "registro-${java.util.UUID.randomUUID()}.db"
        val repo = EscenarioLocal(context, nombreDb)
        val valores =
            mapOf(
                "nombre" to "Persona Prueba",
                "rut" to "11.111.111-1",
                "empresa" to "76.482.915-8",
                "correo" to "prueba@empresa.cl",
                "clave" to "Prueba2026!",
                "confirmar" to "Prueba2026!",
            )
        // Los RUT del caso original son identificadores ficticios; se usa una empresa con RUT
        // verificado para la prueba.
        repo.login("respinoza@induztek.ejemplo", "Induztek2026!")
        repo.cliente(
            cl.duocuc.induztek.model.Cliente(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                nombre = "Empresa QA",
                rut = "22.222.222-2",
                giro = "Prueba",
                contacto = "QA",
                telefono = "912345678",
                correo = "qa@empresa.cl",
            )
        )
        repo.salir()
        val codigo = repo.solicitarCuenta(valores + ("empresa" to "22.222.222-2"))
        assertTrue(codigo.isNotBlank())
        assertThrows(IllegalArgumentException::class.java) {
            repo.login("prueba@empresa.cl", "Prueba2026!")
        }
        repo.login("respinoza@induztek.ejemplo", "Induztek2026!")
        val u = repo.datos.value.usuarios.first { it.correo == "prueba@empresa.cl" }
        assertEquals(Rol.CLIENTE, u.rol)
        assertFalse(u.activo)
        repo.usuario(u.copy(activo = true))
        repo.salir()
        val reiniciado = EscenarioLocal(context, nombreDb)
        assertThrows(IllegalArgumentException::class.java) {
            reiniciado.recuperarCuenta("prueba@empresa.cl", "incorrecto", "Nueva2026!!")
        }
        reiniciado.recuperarCuenta("prueba@empresa.cl", codigo, "Nueva2026!!")
        assertThrows(IllegalArgumentException::class.java) {
            reiniciado.login("prueba@empresa.cl", "Prueba2026!")
        }
        reiniciado.login("prueba@empresa.cl", "Nueva2026!!")
        assertEquals(u.id, reiniciado.actual().id)
        assertEquals(Rol.CLIENTE, reiniciado.actual().rol)
    }
}
