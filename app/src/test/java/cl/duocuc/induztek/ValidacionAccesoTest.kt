package cl.duocuc.induztek

import cl.duocuc.induztek.viewmodel.ValidacionAcceso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionAccesoTest {
    @Test
    fun registroVacioIndicaCamposPendientes() {
        assertEquals(
            setOf("nombre", "rut", "empresa", "correo", "clave", "confirmar"),
            ValidacionAcceso.validar(true, emptyMap()).keys,
        )
    }

    @Test
    fun recuperacionRevisaFormatoYConfirmacion() {
        val errores = ValidacionAcceso.validar(
            false,
            mapOf("correo" to "correo-invalido", "clave" to "clave de prueba", "confirmar" to "otra clave"),
        )
        assertEquals(setOf("correo", "codigo", "clave", "confirmar"), errores.keys)
    }

    @Test
    fun recuperacionValidaNoSolicitaCamposDeRegistro() {
        assertTrue(
            ValidacionAcceso.validar(
                false,
                mapOf(
                    "correo" to "persona@example.test",
                    "codigo" to "codigo-de-prueba",
                    "clave" to "clave de prueba",
                    "confirmar" to "clave de prueba",
                ),
            ).isEmpty()
        )
    }
}
