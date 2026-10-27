package cl.duocuc.induztek.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.viewmodel.*
import java.time.LocalDate

class FormularioViewModel(
    private val sesion: SesionViewModel,
    private val protocolos: ProtocolosViewModel,
) : ViewModel() {
    val ui
        get() = sesion.ui

    private val db
        get() = sesion.db

    private fun validar(f: Formulario, d: Datos) =
        ValidacionFormularios.validar(
            f.tipo,
            f.valores,
            f.campos.filter { it.obligatorio }.map { it.clave },
            f.campos
                .filter { it.selector }
                .associate { it.clave to it.opciones.map { par -> par.first }.toSet() },
            d,
            f.ref,
            f.extra,
        )

    fun valor(k: String, v: String) {
        sesion.actualizar { estado ->
            estado.copy(
                formulario =
                    estado.formulario?.let { f ->
                        val actualizado = f.copy(valores = f.valores + (k to v))
                        actualizado.copy(
                            errores =
                                if (f.errores.isEmpty()) emptyMap()
                                else validar(actualizado, estado.datos)
                        )
                    }
            )
        }
    }

    fun abrir(tipo: String, ref: String = "", extra: String = "") {
        val d = ui.value.datos
        fun c(k: String, e: String, ini: String = "", req: Boolean = true, num: Boolean = false) =
            Campo(k, e, obligatorio = req, numero = num, inicial = ini)
        fun sel(
            k: String,
            e: String,
            opciones: List<Pair<String, String>>,
            ini: String = "",
            req: Boolean = true,
        ) =
            Campo(
                k,
                e,
                opciones,
                obligatorio = req,
                inicial = ini.ifBlank { opciones.firstOrNull()?.first.orEmpty() },
                selector = true,
            )
        val titulo: String
        val campos: List<Campo>
        when (tipo) {
            "cliente" -> {
                titulo = "Registrar cliente"
                campos =
                    listOf(
                        c("nombre", "Razón social"),
                        c("rut", "RUT"),
                        c("giro", "Giro"),
                        c("contacto", "Contacto principal"),
                        c("telefono", "Teléfono"),
                        c("correo", "Correo"),
                    )
            }
            "instalacion" -> {
                titulo = "Registrar instalación"
                campos =
                    listOf(
                        sel(
                            "cliente",
                            "Cliente",
                            d.clientes.map { it.id.toString() to it.nombre },
                            ref,
                        ),
                        c("nombre", "Nombre"),
                        c("ubicacion", "Ubicación"),
                        c("comuna", "Comuna"),
                        c("region", "Región", "Metropolitana"),
                        c("acceso", "Observaciones de acceso", req = false),
                    )
            }
            "equipo" -> {
                titulo = "Registrar equipo"
                campos =
                    listOf(
                        sel(
                            "instalacion",
                            "Instalación",
                            d.instalaciones.map { it.id.toString() to it.nombre },
                            ref,
                        ),
                        c("tag", "TAG único por instalación"),
                        sel("tipo", "Tipo de equipo", Tipo.entries.map { it.name to it.texto }),
                        c("marca", "Marca"),
                        c("modelo", "Modelo", req = false),
                        c("serie", "Número de serie"),
                        c("anio", "Año de fabricación", num = true),
                        c("placa", "Datos de placa: potencia, tensiones, corriente"),
                        c("max", "Máximo de contactos (µΩ); 0 si no aplica", "0", num = true),
                        c("obs", "Observaciones", req = false),
                    )
            }
            "instrumento" -> {
                titulo = "Instrumento y calibración"
                val i = d.instrumentos.firstOrNull { it.id.toString() == ref }
                campos =
                    listOf(
                        c("nombre", "Nombre", i?.nombre.orEmpty()),
                        c("marca", "Marca", i?.marca.orEmpty()),
                        c("modelo", "Modelo", i?.modelo.orEmpty()),
                        c("serie", "Serie", i?.serie.orEmpty()),
                        c("desde", "Calibración (AAAA-MM-DD)", LocalDate.now().toString()),
                        c("hasta", "Vence (AAAA-MM-DD)", LocalDate.now().plusYears(1).toString()),
                    )
            }
            "visita" -> {
                titulo = "Iniciar visita"
                campos =
                    listOf(
                        sel(
                            "instalacion",
                            "Instalación",
                            d.instalaciones.map { it.id.toString() to it.nombre },
                            ref,
                        ),
                        c("ot", "Número OT"),
                        sel(
                            "ingeniero",
                            "Ingeniero a cargo",
                            d.usuarios
                                .filter { it.rol == Rol.INGENIERO && it.activo }
                                .map { it.id.toString() to it.nombre },
                        ),
                        sel(
                            "mantenimiento",
                            "Tipo de visita",
                            listOf("no" to "Solo pruebas", "si" to "Pruebas y mantenimiento"),
                        ),
                        c("fecha", "Fecha (AAAA-MM-DD)", LocalDate.now().toString()),
                    )
            }
            "protocolo" -> {
                titulo = "Crear protocolo"
                val visita = d.visitas.first { it.id.toString() == ref }
                val equipos =
                    d.equipos.filter { e ->
                        e.instalacionId == visita.instalacionId &&
                            d.protocolos.none {
                                it.visitaId.toString() == ref && it.equipoId == e.id
                            }
                    }
                campos =
                    listOf(
                        sel("equipo", "Equipo", equipos.map { it.id.toString() to it.tag }),
                        c("temperatura", "Temperatura ambiente (°C)", num = true),
                        c("humedad", "Humedad relativa (%)", num = true),
                        c("mantenimiento", "Descripción del mantenimiento", req = false),
                        c("obs", "Observaciones", req = false),
                        c("reemplaza", "Nº formalizado al que reemplaza (opcional)", req = false),
                    )
            }
            "editar" -> {
                titulo = "Datos del protocolo"
                val p = d.protocolos.first { it.id.toString() == ref }
                campos =
                    listOf(
                        c(
                            "temperatura",
                            "Temperatura ambiente (°C)",
                            p.temperatura.toString(),
                            num = true,
                        ),
                        c("humedad", "Humedad relativa (%)", p.humedad.toString(), num = true),
                        c("mantenimiento", "Mantenimiento", p.mantenimiento, req = false),
                        c("obs", "Observaciones", p.observaciones, req = false),
                    )
            }
            "medicion" -> {
                val p = d.protocolos.first { it.id.toString() == ref }
                val e = d.equipos.first { it.id == p.equipoId }
                val prueba = Prueba.valueOf(extra.substringBefore("|"))
                val elemento = extra.substringAfter("|")
                val anterior =
                    p.mediciones.lastOrNull { it.prueba == prueba && it.elemento == elemento }
                titulo = "${prueba.texto} · $elemento"
                val momentos =
                    if (d.visitas.first { it.id == p.visitaId }.mantenimiento)
                        listOf("Única", "Previa", "Posterior")
                    else listOf("Única")
                campos =
                    listOf(
                        sel(
                            "realizada",
                            "Estado de la prueba",
                            listOf("si" to "Realizada", "no" to "No realizada"),
                            if (anterior?.realizada == false) "no" else "si",
                        ),
                        c(
                            "motivo",
                            "Motivo si no realizada",
                            anterior?.motivo.orEmpty(),
                            req = false,
                        ),
                        sel(
                            "momento",
                            "Momento",
                            momentos.map { it to it },
                            anterior?.momento.orEmpty(),
                        ),
                        sel(
                            "instrumento",
                            "Instrumento calibrado",
                            d.instrumentos
                                .filter { it.vigente(p.fecha.take(10)) }
                                .map { it.id.toString() to "${it.nombre} · ${it.serie}" },
                            anterior?.instrumentoId?.toString().orEmpty(),
                            req = false,
                        ),
                    ) +
                        Reglas.parametros(e.tipo, prueba).map {
                            c(
                                it.clave,
                                it.etiqueta,
                                anterior?.valores?.get(it.clave)?.toString().orEmpty(),
                                req = false,
                                num = true,
                            )
                        } +
                        (if (prueba == Prueba.AISLACION)
                            listOf(
                                sel(
                                    "unidad",
                                    "Unidad de ingreso",
                                    listOf("M" to "MΩ", "G" to "GΩ (conversión a MΩ)"),
                                )
                            )
                        else emptyList()) +
                        listOf(
                            c(
                                "comentario",
                                "Comentario",
                                anterior?.comentario.orEmpty(),
                                req = false,
                            )
                        )
            }
            "cerrar" -> {
                titulo = "Recepción del acta y cierre"
                campos =
                    listOf(
                        c("nombre", "Nombre de quien recibe"),
                        c("rut", "RUT del receptor"),
                        c("cargo", "Cargo"),
                    )
            }
            "estado" -> {
                titulo = Estado.valueOf(extra).texto
                campos =
                    listOf(
                        c(
                            "motivo",
                            "Motivo / comentario del ingeniero",
                            req = extra in listOf("OBSERVADO", "ANULADO"),
                        )
                    )
            }
            "usuario" -> {
                titulo = "Crear cuenta de usuario"
                campos =
                    listOf(
                        c("nombre", "Nombre completo"),
                        c("rut", "RUT"),
                        c("correo", "Correo"),
                        sel("rol", "Perfil", Rol.entries.map { it.name to it.texto }),
                        sel(
                            "cliente",
                            "Empresa (solo para rol cliente)",
                            d.clientes.map { it.id.toString() to it.nombre },
                        ),
                        c("clave", "Contraseña inicial (mínimo 10 caracteres)"),
                    )
            }
            else -> error("Formulario desconocido")
        }
        sesion.actualizar {
            it.copy(
                formulario =
                    Formulario(
                        titulo,
                        tipo,
                        ref,
                        extra,
                        campos,
                        campos.associate { it.clave to it.inicial },
                    )
            )
        }
    }

    fun guardar() {
        val f = ui.value.formulario ?: return
        val v = f.valores
        val d = ui.value.datos
        fun s(k: String) = v[k].orEmpty().trim()
        fun n(k: String) = Reglas.numero(s(k)) ?: 0.0
        val errores = validar(f, d)
        if (errores.isNotEmpty()) {
            sesion.actualizar { it.copy(formulario = f.copy(errores = errores)) }
            return
        }
        sesion.accion(errorEnFormulario = true) {
            when (f.tipo) {
                "cliente" ->
                    cliente(
                        Cliente(
                            nombre = s("nombre"),
                            rut = s("rut"),
                            giro = s("giro"),
                            contacto = s("contacto"),
                            telefono = s("telefono"),
                            correo = s("correo"),
                        )
                    )
                "instalacion" ->
                    instalacion(
                        Instalacion(
                            clienteId = s("cliente").toLongOrNull() ?: 0L,
                            nombre = s("nombre"),
                            ubicacion = s("ubicacion"),
                            comuna = s("comuna"),
                            region = s("region"),
                            acceso = s("acceso"),
                        )
                    )
                "equipo" ->
                    equipo(
                        Equipo(
                            instalacionId = s("instalacion").toLongOrNull() ?: 0L,
                            tag = s("tag").uppercase(),
                            tipo = Tipo.valueOf(s("tipo")),
                            marca = s("marca"),
                            modelo = s("modelo"),
                            serie = s("serie"),
                            anio = s("anio").toInt(),
                            placa = s("placa"),
                            maxContactos = n("max"),
                            observaciones = s("obs"),
                        )
                    )
                "instrumento" -> {
                    val old = d.instrumentos.firstOrNull { it.id.toString() == f.ref }
                    instrumento(
                        Instrumento(
                            id = old?.id ?: 0L,
                            nombre = s("nombre"),
                            marca = s("marca"),
                            modelo = s("modelo"),
                            serie = s("serie"),
                            calibraciones =
                                (old?.calibraciones ?: emptyList()) +
                                    Calibracion(s("desde"), s("hasta")),
                        )
                    )
                }
                "visita" ->
                    visita(
                        Visita(
                            instalacionId = s("instalacion").toLongOrNull() ?: 0L,
                            ot = s("ot"),
                            fecha = s("fecha"),
                            ingenieroId = s("ingeniero").toLongOrNull() ?: 0L,
                            participantes = listOf(sesion.actual().id),
                            mantenimiento = s("mantenimiento") == "si",
                        )
                    )
                "protocolo" -> {
                    val reemplaza =
                        if (s("reemplaza").isBlank()) 0L
                        else
                            requireNotNull(
                                    d.protocolos.firstOrNull { it.numero == s("reemplaza") }
                                ) {
                                    "No se encontró el protocolo a reemplazar"
                                }
                                .id
                    protocolo(
                        Protocolo(
                            visitaId = f.ref.toLong(),
                            equipoId = s("equipo").toLongOrNull() ?: 0L,
                            creadorId = sesion.actual().id,
                            fecha =
                                d.visitas.first { it.id.toString() == f.ref }.fecha +
                                    "T" +
                                    java.time.LocalTime.now().toString(),
                            temperatura = n("temperatura"),
                            humedad = n("humedad"),
                            mantenimiento = s("mantenimiento"),
                            observaciones = s("obs"),
                            reemplazaId = reemplaza,
                        )
                    )
                }
                "editar" ->
                    editar(
                        f.ref.toLong(),
                        n("temperatura"),
                        n("humedad"),
                        s("mantenimiento"),
                        s("obs"),
                    )
                "medicion" -> {
                    val p = d.protocolos.first { it.id.toString() == f.ref }
                    val e = d.equipos.first { it.id == p.equipoId }
                    val pr = Prueba.valueOf(f.extra.substringBefore("|"))
                    val realizada = s("realizada") == "si"
                    val valores =
                        if (realizada)
                            Reglas.parametros(e.tipo, pr).associate {
                                it.clave to
                                    (n(it.clave) *
                                        if (it.clave in listOf("r1", "r10") && s("unidad") == "G")
                                            1000
                                        else 1)
                            }
                        else emptyMap()
                    medicion(
                        f.ref.toLong(),
                        Medicion(
                            prueba = pr,
                            elemento = f.extra.substringAfter("|"),
                            momento = s("momento"),
                            realizada = realizada,
                            motivo = s("motivo"),
                            instrumentoId = if (realizada) s("instrumento").toLong() else 0L,
                            valores = valores,
                            comentario = s("comentario"),
                        ),
                    )
                }
                "cerrar" -> cerrar(f.ref.toLong(), Recepcion(s("nombre"), s("rut"), s("cargo")))
                "estado" ->
                    protocolos.cambiarEstado(f.ref.toLong(), Estado.valueOf(f.extra), s("motivo"))
                "usuario" -> {
                    val sal = Seguridad.sal()
                    protocolos.actualizarUsuario(
                        Usuario(
                            nombre = s("nombre"),
                            rut = s("rut"),
                            correo = s("correo"),
                            rol = Rol.valueOf(s("rol")),
                            clienteId = if (s("rol") == "CLIENTE") s("cliente").toLong() else 0L,
                            hash = Seguridad.hash(s("clave"), sal),
                            sal = sal,
                        )
                    )
                }
            }
        }
    }

    private fun personal() {
        require(sesion.actual().rol != Rol.CLIENTE) { "Acceso de solo consulta" }
    }

    private fun normalRut(s: String) = s.replace(".", "").replace("-", "").uppercase()

    internal fun cliente(c: Cliente) {
        personal()
        require(Reglas.rut(c.rut)) { "RUT inválido" }
        require(Reglas.correo(c.correo)) { "Correo inválido" }
        require(sesion.datos.value.clientes.none { normalRut(it.rut) == normalRut(c.rut) }) {
            "Cliente con RUT ya registrado"
        }
        db.insertarCliente(c)
        sesion.recargar()
    }

    internal fun instalacion(i: Instalacion) {
        personal()
        require(sesion.datos.value.clientes.any { it.id == i.clienteId })
        db.insertarInstalacion(i)
        sesion.recargar()
    }

    internal fun equipo(e: Equipo) {
        personal()
        val d = sesion.datos.value
        require(d.instalaciones.any { it.id == e.instalacionId })
        require(
            d.equipos.none { it.instalacionId == e.instalacionId && it.tag.equals(e.tag, true) }
        ) {
            "TAG duplicado en esta instalación"
        }
        db.insertarEquipo(e)
        sesion.recargar()
    }

    internal fun instrumento(i: Instrumento) {
        require(sesion.actual().rol == Rol.JEFE) {
            "Solo el jefe técnico puede gestionar instrumentos"
        }
        val d = sesion.datos.value
        require(d.instrumentos.none { it.id != i.id && it.serie.equals(i.serie, true) }) {
            "Serie ya registrada"
        }
        require(i.calibraciones.all { LocalDate.parse(it.desde) <= LocalDate.parse(it.hasta) }) {
            "Calibración inválida"
        }
        if (i.id == 0L) db.insertarInstrumento(i) else db.actualizarInstrumento(i)
        sesion.recargar()
    }

    internal fun visita(v: Visita) {
        personal()
        val d = sesion.datos.value
        require(d.instalaciones.any { it.id == v.instalacionId })
        require(d.usuarios.any { it.id == v.ingenieroId && it.rol == Rol.INGENIERO && it.activo }) {
            "Selecciona un ingeniero activo"
        }
        require(d.visitas.none { it.ot.equals(v.ot, true) }) { "OT ya registrada" }
        db.insertarVisita(v)
        sesion.recargar()
    }

    internal fun protocolo(p: Protocolo) {
        personal()
        val d = sesion.datos.value
        val v = d.visitas.first { it.id == p.visitaId }
        val e = d.equipos.first { it.id == p.equipoId }
        require(v.cierre.isBlank()) { "La visita ya está cerrada" }
        require(e.instalacionId == v.instalacionId)
        require(p.creadorId == sesion.actual().id)
        require(d.protocolos.none { it.visitaId == p.visitaId && it.equipoId == p.equipoId }) {
            "El equipo ya tiene protocolo en esta visita"
        }
        if (p.reemplazaId > 0L)
            require(
                d.protocolos.any {
                    it.id == p.reemplazaId &&
                        it.equipoId == p.equipoId &&
                        it.estado == Estado.FORMALIZADO
                }
            ) {
                "Solo se reemplaza un formalizado del mismo equipo"
            }
        db.insertarProtocolo(
            p.copy(historial = listOf(Cambio(sesion.actual().id, Estado.BORRADOR)))
        )
        sesion.recargar()
    }

    private fun reemplazar(p: Protocolo, motivo: String) {
        val cambiado =
            p.copy(historial = p.historial + Cambio(sesion.actual().id, p.estado, motivo = motivo))
        db.actualizarProtocolo(cambiado)
        sesion.recargar()
    }

    internal fun editar(
        id: Long,
        temperatura: Double,
        humedad: Double,
        mantenimiento: String,
        observaciones: String,
    ) {
        val p = sesion.datos.value.protocolos.first { it.id == id }
        require(Reglas.editable(sesion.actual(), p)) {
            "Solo el autor edita un borrador u observado"
        }
        require(temperatura in -50.0..80.0 && humedad in 0.0..100.0)
        reemplazar(
            p.copy(
                temperatura = temperatura,
                humedad = humedad,
                mantenimiento = mantenimiento,
                observaciones = observaciones,
            ),
            "Datos del protocolo actualizados",
        )
    }

    internal fun medicion(id: Long, m: Medicion) {
        val d = sesion.datos.value
        val p = d.protocolos.first { it.id == id }
        val e = d.equipos.first { it.id == p.equipoId }
        require(Reglas.editable(sesion.actual(), p)) { "El protocolo no admite edición" }
        require(
            m.prueba in Reglas.pruebas(e.tipo) && m.elemento in Reglas.elementos(e.tipo, m.prueba)
        )
        if (m.realizada) {
            require(
                Reglas.validarMedicion(
                        e.tipo,
                        m.prueba,
                        m.valores.mapValues { it.value.toString() },
                        true,
                        "",
                    )
                    .isEmpty()
            ) {
                "Valores de medición inválidos"
            }
            require(
                d.instrumentos.any { it.id == m.instrumentoId && it.vigente(p.fecha.take(10)) }
            ) {
                "Instrumento con calibración vencida a la fecha de prueba"
            }
        } else require(m.motivo.isNotBlank()) { "Motivo obligatorio" }
        require(m.momento in listOf("Única", "Previa", "Posterior"))
        if (m.momento != "Única")
            require(d.visitas.first { it.id == p.visitaId }.mantenimiento) {
                "La visita no incluye mantenimiento"
            }
        val resultado =
            if (m.realizada) Reglas.evaluar(e.tipo, m.prueba, m.valores, e.maxContactos) else null
        val nueva = m.copy(resultado = resultado)
        val existentes =
            p.mediciones.filterNot {
                it.prueba == m.prueba && it.elemento == m.elemento && it.momento == m.momento
            }
        // No mezclar una medición única con el par previa/posterior para el mismo elemento.
        require(
            existentes.none {
                it.prueba == m.prueba &&
                    it.elemento == m.elemento &&
                    (it.momento == "Única" || m.momento == "Única")
            }
        ) {
            "Usa previa/posterior o única para este elemento"
        }
        reemplazar(
            p.copy(mediciones = existentes + nueva),
            "Medición ${m.prueba.texto} / ${m.elemento} / ${m.momento}",
        )
    }

    internal fun cerrar(id: Long, r: Recepcion) {
        require(sesion.actual().rol in listOf(Rol.TECNICO, Rol.INGENIERO)) {
            "Solo técnico o ingeniero cierra visitas"
        }
        require(Reglas.rut(r.rut) && r.nombre.isNotBlank() && r.cargo.isNotBlank()) {
            "Datos del receptor inválidos"
        }
        val d = sesion.datos.value
        val v = d.visitas.first { it.id == id }
        val ps = d.protocolos.filter { it.visitaId == id }
        require(v.cierre.isBlank()) { "Visita ya cerrada" }
        require(
            ps.isNotEmpty() && ps.all { it.estado in listOf(Estado.REGISTRADO, Estado.ANULADO) }
        ) {
            "Todos los protocolos deben estar registrados o anulados"
        }
        val cerrada = v.copy(cierre = ahora(), recepcion = r)
        val temporal = d.copy(visitas = d.visitas.map { if (it.id == id) cerrada else it })
        val acta = cl.duocuc.induztek.native.Documentos.lineas(temporal, visitaId = id)
        db.actualizarVisita(cerrada.copy(acta = acta))
        sesion.recargar()
    }
}
