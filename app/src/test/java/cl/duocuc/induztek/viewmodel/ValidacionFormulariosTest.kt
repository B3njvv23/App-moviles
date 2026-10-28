package cl.duocuc.induztek.viewmodel

import cl.duocuc.induztek.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ValidacionFormulariosTest {
    private val hoy = LocalDate.parse("2026-10-21")
    private val cli =
        Cliente(
            109L,
            "Empresa",
            "76.482.915-8",
            "Industria",
            "Contacto",
            "+56900000101",
            "contacto@empresa.ejemplo",
        )
    private val ingeniera =
        Usuario(107L, "Ingeniera", "15.906.334-8", "ingeniera@empresa.ejemplo", Rol.INGENIERO)
    private val instalacion =
        Instalacion(106L, 109L, "Planta", "Ubicación", "Lampa", "Metropolitana", "")
    private val equipo =
        Equipo(
            102L,
            106L,
            "TR-01",
            Tipo.TRANSFORMADOR,
            "Marca",
            "Modelo",
            "SER-1",
            2020,
            "1500 kVA",
        )
    private val instrumento =
        Instrumento(
            108L,
            "Megóhmetro",
            "Marca",
            "Modelo",
            "CAL-1",
            listOf(Calibracion("2026-01-01", "2026-12-31")),
        )
    private val visita =
        Visita(101L, 106L, "OT-1", "2026-10-21", 107L, listOf(105L), mantenimiento = true)
    private val protocolo =
        Protocolo(104L, "PT-1", 101L, 102L, 105L, "2026-10-21T10:00:00", 20.0, 50.0)
    private val d =
        Datos(
            usuarios = listOf(ingeniera),
            clientes = listOf(cli),
            instalaciones = listOf(instalacion),
            equipos = listOf(equipo),
            instrumentos = listOf(instrumento),
            visitas = listOf(visita),
            protocolos = listOf(protocolo),
        )

    private fun validar(
        t: String,
        v: Map<String, String>,
        requeridos: List<String> = emptyList(),
        ref: String = "",
        extra: String = "",
        datos: Datos = d,
    ) =
        ValidacionFormularios.validar(
            t,
            v,
            requeridos,
            datos = datos,
            ref = ref,
            extra = extra,
            hoy = hoy,
        )

    private fun exige(e: Map<String, String>, vararg campos: String) {
        campos.forEach { assertTrue("Falta error visible en $it: $e", e.containsKey(it)) }
    }

    @Test
    fun loginVacioCorreoInvalidoYCorreccion() {
        exige(
            validar("login", mapOf("correo" to "", "clave" to ""), listOf("correo", "clave")),
            "correo",
            "clave",
        )
        exige(validar("login", mapOf("correo" to "sin-arroba", "clave" to "secreto")), "correo")
        assertTrue(
            validar("login", mapOf("correo" to "correo@empresa.ejemplo", "clave" to "secreto"))
                .isEmpty()
        )
    }

    @Test
    fun clienteCamposObligatoriosTelefonoRutYCorreo() {
        val requeridos = listOf("nombre", "rut", "giro", "contacto", "telefono", "correo")
        exige(validar("cliente", emptyMap(), requeridos), *requeridos.toTypedArray())
        exige(
            validar(
                "cliente",
                mapOf("rut" to "76482915-8", "correo" to "correo malo", "telefono" to "abcdef"),
            ),
            "rut",
            "correo",
            "telefono",
        )
        assertTrue(
            validar(
                    "cliente",
                    mapOf(
                        "nombre" to "Nuevo",
                        "rut" to "17.284.519-3",
                        "correo" to "nuevo@empresa.ejemplo",
                        "telefono" to "+56 9 0000 0000",
                    ),
                )
                .isEmpty()
        )
        exige(validar("cliente", mapOf("telefono" to "123")), "telefono")
    }

    @Test
    fun instalacionSinClienteYDatosObligatorios() {
        exige(
            validar(
                "instalacion",
                emptyMap(),
                listOf("cliente", "nombre", "ubicacion", "comuna", "region"),
            ),
            "cliente",
            "nombre",
            "ubicacion",
            "comuna",
            "region",
        )
        assertTrue(
            validar(
                    "instalacion",
                    mapOf(
                        "cliente" to "109",
                        "nombre" to "Nueva",
                        "ubicacion" to "Acceso sur",
                        "comuna" to "Lampa",
                        "region" to "Metropolitana",
                    ),
                )
                .isEmpty()
        )
    }

    @Test
    fun equipoDuplicadoAnioNoEnteroYMaximoDePlaca() {
        exige(
            validar(
                "equipo",
                mapOf(
                    "instalacion" to "106",
                    "tag" to "tr-01",
                    "tipo" to "CELDA",
                    "anio" to "2026.5",
                    "max" to "0",
                ),
            ),
            "tag",
            "anio",
            "max",
        )
        assertTrue(
            validar(
                    "equipo",
                    mapOf(
                        "instalacion" to "106",
                        "tag" to "TR-02",
                        "tipo" to "TRANSFORMADOR",
                        "anio" to "2020",
                        "max" to "0",
                    ),
                )
                .isEmpty()
        )
        exige(
            validar("equipo", mapOf("instalacion" to "otra", "anio" to "3000", "max" to "NaN")),
            "instalacion",
            "anio",
            "max",
        )
    }

    @Test
    fun instrumentoSerieDuplicadaFechasInvertidasEImposibles() {
        exige(
            validar(
                "instrumento",
                mapOf("serie" to "cal-1", "desde" to "2026-10-20", "hasta" to "2026-10-19"),
            ),
            "serie",
            "hasta",
        )
        exige(
            validar("instrumento", mapOf("desde" to "2026-02-30", "hasta" to "2026-13-01")),
            "desde",
            "hasta",
        )
        exige(
            validar("instrumento", mapOf("desde" to "2026-10-22", "hasta" to "2027-10-22")),
            "desde",
        )
        assertTrue(
            validar(
                    "instrumento",
                    mapOf("serie" to "CAL-1", "desde" to "2026-10-21", "hasta" to "2027-10-21"),
                    ref = "108",
                )
                .isEmpty()
        )
    }

    @Test
    fun visitaOtIngenieroInstalacionYFecha() {
        exige(
            validar(
                "visita",
                mapOf(
                    "ot" to "ot-1",
                    "instalacion" to "otra",
                    "ingeniero" to "desactivado",
                    "fecha" to "2026-10-22",
                ),
            ),
            "ot",
            "instalacion",
            "ingeniero",
            "fecha",
        )
        assertTrue(
            validar(
                    "visita",
                    mapOf(
                        "ot" to "OT-2",
                        "instalacion" to "106",
                        "ingeniero" to "107",
                        "fecha" to "2026-10-21",
                    ),
                )
                .isEmpty()
        )
    }

    @Test
    fun protocoloEquipoAmbienteMantenimientoYReemplazo() {
        exige(
            validar(
                "protocolo",
                mapOf(
                    "equipo" to "102",
                    "temperatura" to "81",
                    "humedad" to "-1",
                    "reemplaza" to "NO-EXISTE",
                ),
                ref = "101",
            ),
            "equipo",
            "temperatura",
            "humedad",
            "mantenimiento",
            "reemplaza",
        )
        val d2 = d.copy(protocolos = emptyList())
        assertTrue(
            validar(
                    "protocolo",
                    mapOf(
                        "equipo" to "102",
                        "temperatura" to "20,5",
                        "humedad" to "50",
                        "mantenimiento" to "Limpieza",
                    ),
                    ref = "101",
                    datos = d2,
                )
                .isEmpty()
        )
    }

    @Test
    fun edicionValidaYErroresPorCampo() {
        exige(
            validar(
                "editar",
                mapOf("temperatura" to "NaN", "humedad" to "101", "mantenimiento" to ""),
                ref = "104",
            ),
            "temperatura",
            "humedad",
            "mantenimiento",
        )
        assertTrue(
            validar(
                    "editar",
                    mapOf("temperatura" to "-5", "humedad" to "0", "mantenimiento" to "Limpieza"),
                    ref = "104",
                )
                .isEmpty()
        )
    }

    @Test
    fun medicionVaciaNegativosInstrumentoVencidoYNoRealizada() {
        exige(
            validar(
                "medicion",
                mapOf(
                    "realizada" to "si",
                    "momento" to "Única",
                    "instrumento" to "no-existe",
                    "v" to "0",
                    "r1" to "-1",
                    "r10" to "NaN",
                ),
                ref = "104",
                extra = "AISLACION|AT-T",
            ),
            "instrumento",
            "v",
            "r1",
            "r10",
        )
        exige(
            validar(
                "medicion",
                mapOf("realizada" to "no", "momento" to "Única", "motivo" to ""),
                ref = "104",
                extra = "AISLACION|AT-T",
            ),
            "motivo",
        )
        assertTrue(
            validar(
                    "medicion",
                    mapOf(
                        "realizada" to "no",
                        "momento" to "Única",
                        "motivo" to "Cliente no autorizó",
                    ),
                    ref = "104",
                    extra = "AISLACION|AT-T",
                )
                .isEmpty()
        )
        assertTrue(
            validar(
                    "medicion",
                    mapOf(
                        "realizada" to "si",
                        "momento" to "Única",
                        "instrumento" to "108",
                        "v" to "5",
                        "r1" to "1000",
                        "r10" to "2500",
                    ),
                    ref = "104",
                    extra = "AISLACION|AT-T",
                )
                .isEmpty()
        )
    }

    @Test
    fun conversionDeUnidadesNoPermiteInfinitoYMomentoNoSeMezcla() {
        exige(
            validar(
                "medicion",
                mapOf(
                    "realizada" to "si",
                    "momento" to "Única",
                    "instrumento" to "108",
                    "v" to "5",
                    "r1" to "1e308",
                    "r10" to "1e308",
                    "unidad" to "G",
                ),
                ref = "104",
                extra = "AISLACION|AT-T",
            ),
            "r1",
            "r10",
        )
        val med =
            Medicion(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                prueba = Prueba.AISLACION,
                elemento = "AT-T",
                momento = "Única",
            )
        exige(
            validar(
                "medicion",
                mapOf("realizada" to "no", "momento" to "Posterior", "motivo" to "No autorizada"),
                ref = "104",
                extra = "AISLACION|AT-T",
                datos = d.copy(protocolos = listOf(protocolo.copy(mediciones = listOf(med)))),
            ),
            "momento",
        )
    }

    @Test
    fun cierreReceptorYProtocolosPendientes() {
        exige(
            validar(
                "cerrar",
                mapOf("nombre" to "", "rut" to "123", "cargo" to ""),
                listOf("nombre", "rut", "cargo"),
                ref = "101",
            ),
            "nombre",
            "rut",
            "cargo",
            "_general",
        )
        val listo = d.copy(protocolos = listOf(protocolo.copy(estado = Estado.REGISTRADO)))
        assertTrue(
            validar(
                    "cerrar",
                    mapOf("nombre" to "Receptor", "rut" to "17.284.519-3", "cargo" to "Encargado"),
                    listOf("nombre", "rut", "cargo"),
                    ref = "101",
                    datos = listo,
                )
                .isEmpty()
        )
    }

    @Test
    fun observacionAnulacionYComentarioDeVariacion() {
        exige(
            validar(
                "estado",
                mapOf("motivo" to ""),
                listOf("motivo"),
                ref = "104",
                extra = "OBSERVADO",
            ),
            "motivo",
        )
        exige(
            validar(
                "estado",
                mapOf("motivo" to ""),
                listOf("motivo"),
                ref = "104",
                extra = "ANULADO",
            ),
            "motivo",
        )
        fun med(r: Double) =
            Medicion(
                id = cl.duocuc.induztek.nuevoIdPrueba(),
                prueba = Prueba.AISLACION,
                elemento = "AT-T",
                valores = mapOf("r1" to r),
                resultado = Resultado.OBSERVADO,
            )
        val previo =
            protocolo.copy(
                id = 110L,
                fecha = "2025-01-01T10:00:00",
                estado = Estado.FORMALIZADO,
                mediciones = listOf(med(1000.0)),
            )
        val actual = protocolo.copy(estado = Estado.REGISTRADO, mediciones = listOf(med(500.0)))
        val datos =
            d.copy(
                visitas = listOf(visita.copy(cierre = "2026-10-21T12:00:00")),
                protocolos = listOf(previo, actual),
            )
        exige(
            validar(
                "estado",
                mapOf("motivo" to ""),
                ref = "104",
                extra = "FORMALIZADO",
                datos = datos,
            ),
            "motivo",
        )
        assertTrue(
            validar(
                    "estado",
                    mapOf("motivo" to "Revisado deterioro"),
                    ref = "104",
                    extra = "FORMALIZADO",
                    datos = datos,
                )
                .isEmpty()
        )
    }

    @Test
    fun usuarioDuplicadosClaveYEmpresaCondicional() {
        exige(
            validar(
                "usuario",
                mapOf(
                    "rut" to ingeniera.rut,
                    "correo" to ingeniera.correo,
                    "clave" to "corta",
                    "rol" to "CLIENTE",
                    "cliente" to "",
                ),
            ),
            "rut",
            "correo",
            "clave",
            "cliente",
        )
        assertTrue(
            validar(
                    "usuario",
                    mapOf(
                        "rut" to "17.284.519-3",
                        "correo" to "nuevo@empresa.ejemplo",
                        "clave" to "ClaveLarga2026!",
                        "rol" to "TECNICO",
                        "cliente" to "",
                    ),
                    listOf("cliente"),
                )
                .isEmpty()
        )
        assertTrue(
            validar(
                    "usuario",
                    mapOf(
                        "rut" to "17.284.519-3",
                        "correo" to "nuevo@empresa.ejemplo",
                        "clave" to "ClaveLarga2026!",
                        "rol" to "CLIENTE",
                        "cliente" to "109",
                    ),
                )
                .isEmpty()
        )
    }

    @Test
    fun selectoresInvalidosYLimitesDeTexto() {
        val e =
            ValidacionFormularios.validar(
                "usuario",
                mapOf("rol" to "INVENTADO", "clave" to "a".repeat(129)),
                emptyList(),
                mapOf("rol" to Rol.entries.map { it.name }.toSet()),
            )
        exige(e, "rol", "clave")
    }
}
