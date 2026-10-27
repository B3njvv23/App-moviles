package cl.duocuc.induztek.viewmodel

import cl.duocuc.induztek.model.Prueba
import cl.duocuc.induztek.model.Resultado
import cl.duocuc.induztek.model.Tipo
import kotlin.math.abs

/** Contrato utilizado por el flujo real de registro de mediciones. null exige revisión manual. */
interface EvaluadorPrueba {
    fun evaluar(tipo: Tipo, valores: Map<String, Double>, maximoPlaca: Double = 0.0): Resultado?
}

/** Comparte la lectura de parámetros entre los criterios automáticos del dominio. */
abstract class EvaluadorAutomatico : EvaluadorPrueba {
    protected fun lectura(valores: Map<String, Double>, clave: String): Double =
        requireNotNull(valores[clave]) { "Falta $clave" }
}

/** Plantilla común para pruebas cuyo resultado depende de una desviación porcentual. */
abstract class EvaluadorPorDesviacion(
    private val limiteAprobado: Double,
    private val limiteObservado: Double,
) : EvaluadorAutomatico() {
    final override fun evaluar(
        tipo: Tipo,
        valores: Map<String, Double>,
        maximoPlaca: Double,
    ): Resultado {
        if (rechazoDirecto(valores)) return Resultado.RECHAZADO
        val error = desviacion(valores)
        return when {
            error <= limiteAprobado + 1e-9 -> Resultado.APROBADO
            error <= limiteObservado + 1e-9 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }

    protected open fun rechazoDirecto(valores: Map<String, Double>) = false

    protected abstract fun desviacion(valores: Map<String, Double>): Double

    protected fun porcentaje(medido: Double, esperado: Double) =
        abs(medido - esperado) / esperado * 100
}

class EvaluadorTtr : EvaluadorPorDesviacion(0.5, 1.0) {
    override fun desviacion(valores: Map<String, Double>) =
        porcentaje(lectura(valores, "medida"), lectura(valores, "teorica"))
}

class EvaluadorRele : EvaluadorPorDesviacion(5.0, 10.0) {
    override fun rechazoDirecto(valores: Map<String, Double>) = lectura(valores, "opera") == 0.0

    override fun desviacion(valores: Map<String, Double>) =
        porcentaje(lectura(valores, "medido"), lectura(valores, "esperado"))
}

class EvaluadorAislacion : EvaluadorAutomatico() {
    override fun evaluar(
        tipo: Tipo,
        valores: Map<String, Double>,
        maximoPlaca: Double,
    ): Resultado? {
        val r1 = lectura(valores, "r1")
        if (r1 < 100) return Resultado.RECHAZADO
        if (tipo !in listOf(Tipo.TRANSFORMADOR, Tipo.CABLE)) return null
        val indice = lectura(valores, "r10") / r1
        return when {
            indice >= 2 -> Resultado.APROBADO
            indice >= 1 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }
}

class EvaluadorContactos : EvaluadorAutomatico() {
    override fun evaluar(tipo: Tipo, valores: Map<String, Double>, maximoPlaca: Double): Resultado {
        require(maximoPlaca > 0) { "Falta el máximo de placa" }
        val resistencia = lectura(valores, "r")
        return when {
            resistencia <= maximoPlaca -> Resultado.APROBADO
            resistencia <= maximoPlaca * 1.2 + 1e-9 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }
}

/** El caso exige que el ingeniero determine el resultado de devanados. */
class EvaluadorManual : EvaluadorPrueba {
    override fun evaluar(
        tipo: Tipo,
        valores: Map<String, Double>,
        maximoPlaca: Double,
    ): Resultado? = null
}

/** Selecciona la estrategia; los consumidores dependen del contrato y no de clases concretas. */
object EvaluadoresPrueba {
    private val evaluadores: Map<Prueba, EvaluadorPrueba> =
        mapOf(
            Prueba.AISLACION to EvaluadorAislacion(),
            Prueba.TTR to EvaluadorTtr(),
            Prueba.CONTACTOS to EvaluadorContactos(),
            Prueba.RELE to EvaluadorRele(),
            Prueba.DEVANADOS to EvaluadorManual(),
        )

    fun para(prueba: Prueba): EvaluadorPrueba = evaluadores.getValue(prueba)
}
