package cl.duocuc.induztek.viewmodel

import cl.duocuc.induztek.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ReglasTest {
    @Test
    fun rutConDigitoVerificador() {
        assertTrue(Reglas.rut("17.284.519-3"))
        assertTrue(Reglas.rut("14.226.791-8"))
        assertFalse(Reglas.rut("17.284.519-4"))
        assertFalse(Reglas.rut("123"))
    }

    @Test
    fun numerosChilenosYValoresNoFinitos() {
        assertEquals(1.25, Reglas.numero("1,25")!!, 0.0)
        assertNull(Reglas.numero("NaN"))
        assertNull(Reglas.numero("Infinity"))
    }

    @Test
    fun correoYFormularioCompleto() {
        assertTrue(Reglas.correo("mgodoy@induztek.ejemplo"))
        assertFalse(Reglas.correo("sin correo"))
        val valores =
            mapOf(
                "rut" to "17.284.519-3",
                "correo" to "mgodoy@induztek.ejemplo",
                "nombre" to "Matías",
            )
        assertTrue(
            Reglas.validarFormulario("cliente", valores, listOf("nombre", "rut", "correo"))
                .isEmpty()
        )
    }

    @Test
    fun aislacionEnLimitesYLecturaMinima() {
        fun evaluar(a: Double, b: Double) =
            Reglas.evaluar(Tipo.TRANSFORMADOR, Prueba.AISLACION, mapOf("r1" to a, "r10" to b))
        assertEquals(Resultado.APROBADO, evaluar(100.0, 200.0))
        assertEquals(Resultado.OBSERVADO, evaluar(100.0, 100.0))
        assertEquals(Resultado.RECHAZADO, evaluar(100.0, 99.0))
        assertEquals(Resultado.RECHAZADO, evaluar(99.0, 400.0))
        assertNull(Reglas.evaluar(Tipo.CELDA, Prueba.AISLACION, mapOf("r1" to 500.0)))
    }

    @Test
    fun ttrEnLimites() {
        fun resultado(n: Double) =
            Reglas.evaluar(Tipo.TRANSFORMADOR, Prueba.TTR, mapOf("teorica" to 100.0, "medida" to n))
        assertEquals(Resultado.APROBADO, resultado(100.5))
        assertEquals(Resultado.OBSERVADO, resultado(101.0))
        assertEquals(Resultado.RECHAZADO, resultado(101.01))
    }

    @Test
    fun contactosYReleNoOpera() {
        assertEquals(
            Resultado.APROBADO,
            Reglas.evaluar(Tipo.CELDA, Prueba.CONTACTOS, mapOf("r" to 60.0), 60.0),
        )
        assertEquals(
            Resultado.OBSERVADO,
            Reglas.evaluar(Tipo.CELDA, Prueba.CONTACTOS, mapOf("r" to 72.0), 60.0),
        )
        assertEquals(
            Resultado.RECHAZADO,
            Reglas.evaluar(Tipo.CELDA, Prueba.CONTACTOS, mapOf("r" to 72.1), 60.0),
        )
        assertEquals(
            Resultado.RECHAZADO,
            Reglas.evaluar(
                Tipo.RELE,
                Prueba.RELE,
                mapOf("esperado" to 100.0, "medido" to 100.0, "opera" to 0.0),
            ),
        )
    }

    @Test
    fun motivoObligatorioParaNoRealizadaYDenominadoresPositivos() {
        assertTrue(
            Reglas.validarMedicion(Tipo.CABLE, Prueba.AISLACION, emptyMap(), false, "")
                .containsKey("motivo")
        )
        assertTrue(
            Reglas.validarMedicion(Tipo.CABLE, Prueba.AISLACION, emptyMap(), false, "No autorizado")
                .isEmpty()
        )
        assertTrue(
            Reglas.validarMedicion(
                    Tipo.TRANSFORMADOR,
                    Prueba.TTR,
                    mapOf("teorica" to "0", "medida" to "30", "tap" to "3"),
                    true,
                    "",
                )
                .containsKey("teorica")
        )
    }

    private fun protocolo(ms: List<Medicion>, fecha: String = "2026-10-21T10:00:00") =
        Protocolo(
            id = cl.duocuc.induztek.nuevoIdPrueba(),
            visitaId = 101L,
            equipoId = 102L,
            creadorId = 100L,
            temperatura = 20.0,
            humedad = 50.0,
            mediciones = ms,
            fecha = fecha,
            estado = Estado.REGISTRADO,
        )

    @Test
    fun posteriorPrevaleceYNoRealizadaNoCuenta() {
        val a =
            Medicion(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                prueba = Prueba.AISLACION,
                elemento = "U",
                momento = "Previa",
                resultado = Resultado.RECHAZADO,
            )
        val b =
            a.copy(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                momento = "Posterior",
                resultado = Resultado.APROBADO,
            )
        val c =
            a.copy(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                elemento = "V",
                realizada = false,
                motivo = "Sin autorización",
                resultado = null,
            )
        assertEquals(Resultado.APROBADO, Reglas.global(protocolo(listOf(a, b, c))))
    }

    @Test
    fun comparacionHistoricaDeAnexoNueve() {
        fun m(a: Double) =
            Medicion(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                prueba = Prueba.AISLACION,
                elemento = "AT-T",
                valores = mapOf("r1" to a),
                resultado = Resultado.OBSERVADO,
            )
        val anterior = protocolo(listOf(m(9800.0)), "2025-09-13T09:00:00")
        val actual = protocolo(listOf(m(6100.0)))
        assertEquals(-37.7551, Reglas.variacion(6100.0, 9800.0)!!, .001)
        assertEquals(
            1,
            Reglas.significativas(actual, Datos(protocolos = listOf(anterior, actual))).size,
        )
        assertNull(Reglas.variacion(10.0, 0.0))
    }

    @Test
    fun plazoDeDosDiasHabilesExcluyeFinDeSemana() {
        val p = protocolo(emptyList(), "2026-10-16T10:00:00") // viernes
        assertFalse(Reglas.atrasado(p, LocalDate.parse("2026-10-20")))
        assertTrue(Reglas.atrasado(p, LocalDate.parse("2026-10-21")))
    }

    @Test
    fun propietarioYEstadoRestringenEdicion() {
        val p = protocolo(emptyList()).copy(estado = Estado.BORRADOR)
        val u = Usuario(100L, "Técnico", "17.284.519-3", "a@b.cl", Rol.TECNICO)
        assertTrue(Reglas.editable(u, p))
        assertFalse(Reglas.editable(u.copy(id = 103L), p))
        assertFalse(Reglas.editable(u, p.copy(estado = Estado.FORMALIZADO)))
        assertFalse(Reglas.editable(u.copy(rol = Rol.CLIENTE), p))
    }
}
