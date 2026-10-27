package cl.duocuc.induztek.viewmodel

import cl.duocuc.induztek.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs

/** Funciones puras: validación y criterios del caso independientes de Compose y Android. */
object Reglas {
    fun numero(s: String) = s.trim().replace(",", ".").toDoubleOrNull()?.takeIf { it.isFinite() }

    fun rut(s: String): Boolean {
        val t = s.replace(".", "").replace("-", "").uppercase().trim()
        if (!t.matches(Regex("[0-9]{7,8}[0-9K]"))) return false
        val suma =
            t.dropLast(1).reversed().mapIndexed { i, c -> c.digitToInt() * (2 + i % 6) }.sum()
        return t.last().toString() ==
            when (val n = 11 - suma % 11) {
                11 -> "0"
                10 -> "K"
                else -> n.toString()
            }
    }

    fun correo(s: String) = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(s)

    fun pruebas(t: Tipo) =
        when (t) {
            Tipo.TRANSFORMADOR -> listOf(Prueba.AISLACION, Prueba.TTR, Prueba.DEVANADOS)
            Tipo.CELDA,
            Tipo.INTERRUPTOR -> listOf(Prueba.AISLACION, Prueba.CONTACTOS)
            Tipo.CABLE -> listOf(Prueba.AISLACION)
            Tipo.RELE -> listOf(Prueba.RELE)
        }

    fun elementos(t: Tipo, p: Prueba) =
        when (p) {
            Prueba.AISLACION ->
                when (t) {
                    Tipo.TRANSFORMADOR -> listOf("AT-BT", "AT-T", "BT-T")
                    Tipo.CABLE -> listOf("U", "V", "W")
                    else -> listOf("Equipo")
                }
            Prueba.TTR -> listOf("H1-X1", "H2-X2", "H3-X3")
            Prueba.DEVANADOS -> listOf("AT-U", "AT-V", "AT-W", "BT-U", "BT-V", "BT-W")
            Prueba.CONTACTOS -> listOf("Polo 1", "Polo 2", "Polo 3")
            Prueba.RELE -> listOf("Operación")
        }

    fun parametros(t: Tipo, p: Prueba): List<Parametro> =
        when (p) {
            Prueba.AISLACION ->
                listOf(
                    Parametro("v", "Tensión de prueba (kV)"),
                    Parametro("r1", "Lectura 1 min (MΩ)"),
                ) +
                    (if (t in listOf(Tipo.TRANSFORMADOR, Tipo.CABLE))
                        listOf(Parametro("r10", "Lectura 10 min (MΩ)"))
                    else emptyList())
            Prueba.TTR ->
                listOf(
                    Parametro("teorica", "Relación teórica"),
                    Parametro("medida", "Relación medida"),
                    Parametro("tap", "Posición tap"),
                )
            Prueba.DEVANADOS -> listOf(Parametro("r", "Resistencia (mΩ)"))
            Prueba.CONTACTOS -> listOf(Parametro("r", "Resistencia (µΩ)"))
            Prueba.RELE ->
                listOf(
                    Parametro("ajuste", "Corriente de ajuste (A)"),
                    Parametro("corriente", "Corriente medida (A)", false),
                    Parametro("esperado", "Tiempo esperado (ms)"),
                    Parametro("medido", "Tiempo medido (ms)", false),
                    Parametro("opera", "Opera: 1 = sí, 0 = no", false),
                )
        }

    fun validarMedicion(
        t: Tipo,
        p: Prueba,
        v: Map<String, String>,
        realizada: Boolean,
        motivo: String,
    ): Map<String, String> {
        if (!realizada)
            return if (motivo.isBlank()) mapOf("motivo" to "Indica por qué no se realizó")
            else emptyMap()
        val e = mutableMapOf<String, String>()
        parametros(t, p).forEach {
            val n = numero(v[it.clave].orEmpty())
            if (n == null || n < 0 || (it.positivo && n == 0.0))
                e[it.clave] =
                    "Ingresa un número ${if(it.positivo)"mayor que cero"else "no negativo"}"
        }
        if (p == Prueba.RELE && numero(v["opera"].orEmpty()) !in listOf(0.0, 1.0))
            e["opera"] = "Usa 1 o 0"
        if (p == Prueba.TTR && numero(v["tap"].orEmpty())?.let { it % 1 != 0.0 } == true)
            e["tap"] = "El tap debe ser entero"
        return e
    }

    fun vigentes(p: Protocolo) =
        p.mediciones
            .groupBy { it.prueba to it.elemento }
            .values
            .map { g ->
                g.firstOrNull { it.momento == "Posterior" }
                    ?: g.firstOrNull { it.momento == "Única" }
                    ?: g.last()
            }

    fun global(p: Protocolo) =
        vigentes(p).filter { it.realizada }.mapNotNull { it.resultado }.maxByOrNull { it.ordinal }

    fun pendiente(p: Protocolo, t: Tipo) =
        pruebas(t).flatMap { pr ->
            elementos(t, pr).mapNotNull { el ->
                if (vigentes(p).none { it.prueba == pr && it.elemento == el }) "${pr.texto} · $el"
                else null
            }
        }

    fun variacion(a: Double, b: Double) = if (b == 0.0) null else (a - b) / abs(b) * 100

    fun significativas(p: Protocolo, d: Datos): List<String> {
        val prev =
            d.protocolos
                .filter {
                    it.equipoId == p.equipoId &&
                        it.fecha < p.fecha &&
                        it.estado !in listOf(Estado.BORRADOR, Estado.ANULADO)
                }
                .sortedByDescending { it.fecha }
        return vigentes(p)
            .filter { it.realizada }
            .flatMap { m ->
                val old = prev.firstNotNullOfOrNull { a ->
                    vigentes(a).firstOrNull {
                        it.prueba == m.prueba && it.elemento == m.elemento && it.realizada
                    }
                }
                m.valores
                    .filterKeys { it !in listOf("v", "tap", "opera") }
                    .mapNotNull { (k, v) ->
                        old?.valores?.get(k)?.let { anterior ->
                            val n = variacion(v, anterior)
                            when {
                                n != null && abs(n) > 25 ->
                                    "${m.prueba.texto} / ${m.elemento} / $k: %.1f %%".format(n)
                                anterior == 0.0 && v != 0.0 ->
                                    "${m.elemento}: referencia cero; revisar cambio"
                                else -> null
                            }
                        }
                    }
            }
    }

    fun atrasado(p: Protocolo, hoy: LocalDate = LocalDate.now()): Boolean {
        if (p.estado in listOf(Estado.FORMALIZADO, Estado.ANULADO)) return false
        var f = LocalDate.parse(p.fecha.take(10))
        var dias = 0
        while (f < hoy) {
            f = f.plusDays(1)
            if (f.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) dias++
        }
        return dias > 2
    }

    fun editable(u: Usuario, p: Protocolo) =
        u.rol != Rol.CLIENTE &&
            u.id == p.creadorId &&
            p.estado in listOf(Estado.BORRADOR, Estado.OBSERVADO)

    fun validarFormulario(
        tipo: String,
        v: Map<String, String>,
        obligatorios: List<String>,
    ): Map<String, String> {
        val e =
            obligatorios
                .filter { v[it].isNullOrBlank() }
                .associateWith { "Campo obligatorio" }
                .toMutableMap()
        fun s(k: String) = v[k].orEmpty()
        if (v.containsKey("rut") && !rut(s("rut"))) e["rut"] = "Dígito verificador de RUT inválido"
        if (v.containsKey("correo") && !correo(s("correo")))
            e["correo"] = "Ingresa un correo válido"
        listOf("fecha", "desde", "hasta")
            .filter { v.containsKey(it) }
            .forEach {
                try {
                    LocalDate.parse(s(it))
                } catch (ex: Exception) {
                    e[it] = "Fecha válida AAAA-MM-DD"
                }
            }
        if (
            v.containsKey("desde") &&
                !e.containsKey("desde") &&
                !e.containsKey("hasta") &&
                s("desde") > s("hasta")
        )
            e["hasta"] = "Debe ser posterior a la calibración"
        if (tipo in listOf("protocolo", "editar")) {
            if (numero(s("temperatura"))?.let { it in -50.0..80.0 } != true)
                e["temperatura"] = "Entre -50 y 80 °C"
            if (numero(s("humedad"))?.let { it in 0.0..100.0 } != true)
                e["humedad"] = "Entre 0 y 100 %"
        }
        if (tipo == "equipo") {
            if (s("anio").toIntOrNull()?.let { it in 1900..LocalDate.now().year } != true)
                e["anio"] = "Año válido entre 1900 y ${LocalDate.now().year}"
            if (numero(s("max"))?.let { it >= 0 } != true) e["max"] = "Número no negativo"
            if (
                s("tipo") in listOf("CELDA", "INTERRUPTOR") &&
                    numero(s("max"))?.let { it > 0 } != true
            )
                e["max"] = "Indica el máximo de placa mayor a cero"
        }
        if (tipo == "usuario" && s("clave").length < 10) e["clave"] = "Mínimo 10 caracteres"
        return e
    }
}
