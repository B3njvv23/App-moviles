package cl.duocuc.induztek.viewmodel

import cl.duocuc.induztek.model.*
import java.time.LocalDate

/** Validación de cada campo, incluidas las reglas que dependen de otros registros. */
object ValidacionFormularios {
    private fun rutNormal(s: String) = s.filter { it.isLetterOrDigit() }.uppercase()

    fun validar(
        tipo: String,
        valores: Map<String, String>,
        obligatorios: List<String>,
        opciones: Map<String, Set<String>> = emptyMap(),
        datos: Datos = Datos(),
        ref: String = "",
        extra: String = "",
        hoy: LocalDate = LocalDate.now(),
    ): Map<String, String> {
        val v = valores.mapValues { it.value.trim() }
        fun s(k: String) = v[k].orEmpty()
        val requeridos = obligatorios.filterNot {
            it == "cliente" && tipo == "usuario" && s("rol") != "CLIENTE"
        }
        val e = Reglas.validarFormulario(tipo, v, requeridos).toMutableMap()
        opciones.forEach { (k, permitidas) ->
            val visible =
                !(tipo == "usuario" && k == "cliente" && s("rol") != "CLIENTE") &&
                    !(tipo == "medicion" &&
                        s("realizada") == "no" &&
                        k in listOf("instrumento", "unidad"))
            if (visible && s(k).isNotBlank() && s(k) !in permitidas)
                e[k] = "Selecciona una opción disponible"
        }
        // Límites de entrada para evitar valores accidentales que luego no caben en el documento.
        v.forEach { (k, texto) ->
            val limite =
                when (k) {
                    "obs",
                    "comentario",
                    "motivo",
                    "mantenimiento",
                    "acceso" -> 2000
                    "clave" -> 128
                    "correo" -> 254
                    "placa",
                    "ubicacion" -> 500
                    else -> 160
                }
            if (texto.length > limite) e[k] = "Máximo $limite caracteres"
        }
        if (tipo == "cliente") {
            if (datos.clientes.any { rutNormal(it.rut) == rutNormal(s("rut")) })
                e["rut"] = "Ya existe un cliente con este RUT"
            if (
                s("telefono").isNotBlank() &&
                    (!s("telefono").matches(Regex("\\+?[0-9 ()-]+")) ||
                        s("telefono").count { it.isDigit() } !in 7..15)
            )
                e["telefono"] = "Ingresa entre 7 y 15 dígitos; puedes usar +, espacios y guiones"
        }
        if (tipo == "instalacion" && datos.clientes.none { it.id.toString() == s("cliente") })
            e["cliente"] = "Selecciona un cliente registrado"
        if (tipo == "equipo") {
            if (datos.instalaciones.none { it.id.toString() == s("instalacion") })
                e["instalacion"] = "Selecciona una instalación registrada"
            if (
                datos.equipos.any {
                    it.instalacionId.toString() == s("instalacion") && it.tag.equals(s("tag"), true)
                }
            )
                e["tag"] = "TAG ya registrado en esta instalación"
        }
        if (tipo == "instrumento") {
            if (
                datos.instrumentos.any {
                    it.id.toString() != ref && it.serie.equals(s("serie"), true)
                }
            )
                e["serie"] = "Ya existe un instrumento con esta serie"
            val desde = runCatching { LocalDate.parse(s("desde")) }.getOrNull()
            if (desde != null && desde > hoy)
                e["desde"] = "La última calibración no puede ser futura"
        }
        if (tipo == "visita") {
            if (datos.visitas.any { it.ot.equals(s("ot"), true) })
                e["ot"] = "Esta OT ya tiene una visita registrada"
            if (datos.instalaciones.none { it.id.toString() == s("instalacion") })
                e["instalacion"] = "Selecciona una instalación registrada"
            if (
                datos.usuarios.none {
                    it.id.toString() == s("ingeniero") && it.rol == Rol.INGENIERO && it.activo
                }
            )
                e["ingeniero"] = "Selecciona un ingeniero activo"
            val fecha = runCatching { LocalDate.parse(s("fecha")) }.getOrNull()
            if (fecha != null && fecha > hoy)
                e["fecha"] = "Registra una visita de hoy o una fecha anterior"
        }
        if (tipo in listOf("protocolo", "editar")) {
            val p = datos.protocolos.firstOrNull { it.id.toString() == ref }
            val visita =
                datos.visitas.firstOrNull {
                    it.id == if (tipo == "protocolo") ref.toLongOrNull() else p?.visitaId
                }
            if (visita?.mantenimiento == true && s("mantenimiento").isBlank())
                e["mantenimiento"] = "Describe el mantenimiento realizado al equipo"
            if (tipo == "protocolo") {
                val equipo =
                    datos.equipos.firstOrNull {
                        it.id.toString() == s("equipo") && it.instalacionId == visita?.instalacionId
                    }
                if (equipo == null) e["equipo"] = "Selecciona un equipo de esta instalación"
                if (
                    datos.protocolos.any {
                        it.visitaId.toString() == ref && it.equipoId.toString() == s("equipo")
                    }
                )
                    e["equipo"] = "El equipo ya tiene un protocolo en esta visita"
                if (visita?.cierre?.isNotBlank() == true)
                    e["_general"] =
                        "La visita está cerrada; inicia otra visita para registrar un protocolo"
                if (
                    s("reemplaza").isNotBlank() &&
                        datos.protocolos.none {
                            it.numero == s("reemplaza") &&
                                it.equipoId.toString() == s("equipo") &&
                                it.estado == Estado.FORMALIZADO
                        }
                )
                    e["reemplaza"] = "Indica un protocolo formalizado del mismo equipo"
            }
        }
        if (tipo == "medicion") {
            val p = datos.protocolos.firstOrNull { it.id.toString() == ref }
            val eq = datos.equipos.firstOrNull { it.id == p?.equipoId }
            val pr = runCatching { Prueba.valueOf(extra.substringBefore('|')) }.getOrNull()
            if (eq == null || pr == null || p == null)
                e["_general"] = "No se encontró el protocolo; vuelve a abrir la medición"
            else {
                val realizada = s("realizada") == "si"
                e.putAll(Reglas.validarMedicion(eq.tipo, pr, v, realizada, s("motivo")))
                if (
                    realizada &&
                        datos.instrumentos.none {
                            it.id.toString() == s("instrumento") && it.vigente(p.fecha.take(10))
                        }
                )
                    e["instrumento"] = "Selecciona un instrumento calibrado a la fecha de prueba"
                if (realizada && s("unidad") == "G")
                    listOf("r1", "r10").forEach { k ->
                        Reglas.numero(s(k))?.let {
                            if (!(it * 1000).isFinite())
                                e[k] = "La lectura es demasiado grande para convertirla a MΩ"
                        }
                    }
                val existentes =
                    p.mediciones.filter {
                        it.prueba == pr &&
                            it.elemento == extra.substringAfter('|') &&
                            it.momento != s("momento")
                    }
                if (existentes.any { it.momento == "Única" || s("momento") == "Única" })
                    e["momento"] =
                        "Este elemento ya usa ${existentes.first().momento}; edítalo o conserva el par previa/posterior"
                if (
                    s("momento") != "Única" &&
                        datos.visitas.firstOrNull { it.id == p.visitaId }?.mantenimiento != true
                )
                    e["momento"] = "Esta visita solo permite una medición única"
            }
        }
        if (tipo == "cerrar") {
            val ps = datos.protocolos.filter { it.visitaId.toString() == ref }
            if (ps.isEmpty() || ps.any { it.estado !in listOf(Estado.REGISTRADO, Estado.ANULADO) })
                e["_general"] =
                    "No se puede cerrar: todos los protocolos deben estar registrados en terreno o anulados"
            if (datos.visitas.firstOrNull { it.id.toString() == ref }?.cierre?.isNotBlank() == true)
                e["_general"] = "La visita ya está cerrada"
        }
        if (tipo == "estado" && extra == "FORMALIZADO") {
            datos.protocolos
                .firstOrNull { it.id.toString() == ref }
                ?.let { p ->
                    if (Reglas.significativas(p, datos).isNotEmpty() && s("motivo").isBlank())
                        e["motivo"] = "Comenta la variación significativa antes de formalizar"
                    if (Reglas.vigentes(p).any { it.realizada && it.resultado == null })
                        e["_general"] =
                            "Asigna primero los resultados manuales pendientes en el protocolo"
                    if (datos.visitas.firstOrNull { it.id == p.visitaId }?.cierre.isNullOrBlank())
                        e["_general"] = "Cierra la visita y entrega el acta antes de formalizar"
                }
        }
        if (tipo == "usuario") {
            if (datos.usuarios.any { it.correo.equals(s("correo"), true) })
                e["correo"] = "Este correo ya tiene una cuenta"
            if (datos.usuarios.any { rutNormal(it.rut) == rutNormal(s("rut")) })
                e["rut"] = "Este RUT ya tiene una cuenta"
            if (s("rol") == "CLIENTE" && datos.clientes.none { it.id.toString() == s("cliente") })
                e["cliente"] = "Selecciona la empresa del usuario cliente"
        }
        return e
    }
}
