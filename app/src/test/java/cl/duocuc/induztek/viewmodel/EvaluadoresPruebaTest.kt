package cl.duocuc.induztek.viewmodel

import cl.duocuc.induztek.model.*
import org.junit.Assert.*
import org.junit.Test

class EvaluadoresPruebaTest {
    @Test
    fun estrategiasSeUsanMedianteElMismoContrato() {
        val casos =
            listOf(
                Triple(
                    Prueba.TTR,
                    mapOf("teorica" to 100.0, "medida" to 100.5),
                    Resultado.APROBADO,
                ),
                Triple(
                    Prueba.RELE,
                    mapOf("esperado" to 100.0, "medido" to 108.0, "opera" to 1.0),
                    Resultado.OBSERVADO,
                ),
                Triple(Prueba.AISLACION, mapOf("r1" to 99.0), Resultado.RECHAZADO),
            )
        casos.forEach { (prueba, valores, esperado) ->
            val evaluador: EvaluadorPrueba = EvaluadoresPrueba.para(prueba)
            assertEquals(esperado, evaluador.evaluar(Tipo.TRANSFORMADOR, valores))
            assertEquals(esperado, Reglas.evaluar(Tipo.TRANSFORMADOR, prueba, valores))
        }
    }

    @Test
    fun plantillaCompartidaRespetaLimitesDeTtrYRele() {
        listOf(
                100.5 to Resultado.APROBADO,
                101.0 to Resultado.OBSERVADO,
                101.01 to Resultado.RECHAZADO,
            )
            .forEach { (medida, esperado) ->
                assertEquals(
                    esperado,
                    Reglas.evaluar(
                        Tipo.TRANSFORMADOR,
                        Prueba.TTR,
                        mapOf("teorica" to 100.0, "medida" to medida),
                    ),
                )
            }
        listOf(
                105.0 to Resultado.APROBADO,
                110.0 to Resultado.OBSERVADO,
                110.01 to Resultado.RECHAZADO,
            )
            .forEach { (medido, esperado) ->
                assertEquals(
                    esperado,
                    Reglas.evaluar(
                        Tipo.RELE,
                        Prueba.RELE,
                        mapOf("esperado" to 100.0, "medido" to medido, "opera" to 1.0),
                    ),
                )
            }
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
    fun noSeInventaAprobacionParaRevisionManual() {
        assertNull(Reglas.evaluar(Tipo.TRANSFORMADOR, Prueba.DEVANADOS, mapOf("r" to 1.0)))
        listOf(Tipo.CELDA, Tipo.INTERRUPTOR).forEach { tipo ->
            assertNull(Reglas.evaluar(tipo, Prueba.AISLACION, mapOf("r1" to 100.0)))
            assertEquals(
                Resultado.RECHAZADO,
                Reglas.evaluar(tipo, Prueba.AISLACION, mapOf("r1" to 99.0)),
            )
        }
    }

    @Test
    fun todosLosTiposDePruebaTienenEstrategia() {
        Prueba.entries.forEach { assertNotNull(EvaluadoresPrueba.para(it)) }
    }

    @Test
    fun contactosExigeMaximoDePlaca() {
        assertThrows(IllegalArgumentException::class.java) {
            Reglas.evaluar(Tipo.CELDA, Prueba.CONTACTOS, mapOf("r" to 60.0))
        }
    }
}
