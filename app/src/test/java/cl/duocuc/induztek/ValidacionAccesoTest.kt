package cl.duocuc.induztek

import cl.duocuc.induztek.viewmodel.ValidacionAcceso
import org.junit.Assert.*
import org.junit.Test

class ValidacionAccesoTest {
    @Test
    fun registroVacioTieneErrorPorCampo() {
        assertEquals(
            setOf("nombre", "rut", "empresa", "correo", "clave", "confirmar"),
            ValidacionAcceso.validar(true, emptyMap()).keys,
        )
    }

    @Test
    fun recuperacionExigeCodigoYConfirmacion() {
        val errores =
            ValidacionAcceso.validar(
                false,
                mapOf("correo" to "mal", "clave" to "corta", "confirmar" to "distinta"),
            )
        assertEquals(setOf("correo", "codigo", "clave", "confirmar"), errores.keys)
    }

    @Test
    fun recuperacionValidaNoExigeDatosDeRegistro() {
        assertTrue(
            ValidacionAcceso.validar(
                    false,
                    mapOf(
                        "correo" to "cliente@empresa.cl",
                        "codigo" to "secreto",
                        "clave" to "Contraseña2026!",
                        "confirmar" to "Contraseña2026!",
                    ),
                )
                .isEmpty()
        )
    }
}
