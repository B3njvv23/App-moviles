package cl.duocuc.induztek.data

import cl.duocuc.induztek.model.*

object Semilla {
    fun crear(): Datos {
        val nombres =
            listOf(
                "Envasadora del Pacífico S.A.",
                "Sanitaria Valle Central S.A.",
                "Minera Cerro Alto SpA",
                "Clínica Los Aromos Ltda.",
                "Cementos Lomas Blancas S.A.",
                "Data Center Andes Norte SpA",
            )
        val ruts =
            listOf(
                "76.482.915-8",
                "96.731.540-0",
                "77.105.362-9",
                "76.903.218-5",
                "99.524.170-6",
                "78.214.635-1",
            )
        val contactos =
            listOf(
                "Marcela Fuentes Rojas",
                "Jorge Arancibia Leiva",
                "Patricio Navarro Soto",
                "Carolina Méndez Pizarro",
                "Luis Bravo Castillo",
                "Felipe Órdenes Vera",
            )
        val clientes = nombres.mapIndexed { i, n ->
            Cliente(
                (i + 1).toLong(),
                n,
                ruts[i],
                listOf(
                    "Agroindustria",
                    "Sanitaria",
                    "Minería",
                    "Salud",
                    "Construcción",
                    "Tecnología",
                )[i],
                contactos[i],
                "+56 9 0000 010${i+1}",
                "contacto$i@cliente.ejemplo",
            )
        }
        val instalaciones =
            listOf(
                    "Planta Lampa",
                    "Planta Elevadora Sur",
                    "Faena Til Til",
                    "Edificio Clínico",
                    "Planta Molienda",
                    "Site Huechuraba",
                )
                .mapIndexed { i, n ->
                    Instalacion(
                        (i + 1).toLong(),
                        (i + 1).toLong(),
                        n,
                        "Sector industrial",
                        listOf(
                            "Lampa",
                            "San Bernardo",
                            "Til Til",
                            "La Florida",
                            "Puente Alto",
                            "Huechuraba",
                        )[i],
                        "Metropolitana",
                        "Confirmar inducción y acceso; posible falta de señal",
                    )
                }
        fun u(i: Int, n: String, r: String, c: String, rol: Rol, cli: Long = 0L): Usuario {
            val sal = Seguridad.sal()
            return Usuario(
                (i + 1).toLong(),
                n,
                r,
                c,
                rol,
                cli,
                hash = Seguridad.hash("Induztek2026!", sal),
                sal = sal,
            )
        }
        val usuarios =
            listOf(
                u(
                    0,
                    "Matías Godoy Herrera",
                    "17.284.519-3",
                    "mgodoy@induztek.ejemplo",
                    Rol.TECNICO,
                ),
                u(1, "Javiera Tapia Muñoz", "19.412.087-7", "jtapia@induztek.ejemplo", Rol.TECNICO),
                u(
                    2,
                    "Carlos Rondón Pérez",
                    "26.104.583-4",
                    "crondon@induztek.ejemplo",
                    Rol.TECNICO,
                ),
                u(
                    3,
                    "Diego Valenzuela Ríos",
                    "20.137.648-3",
                    "dvalenzuela@induztek.ejemplo",
                    Rol.TECNICO,
                ),
                u(
                    4,
                    "Andrea Contreras Silva",
                    "15.906.334-8",
                    "acontreras@induztek.ejemplo",
                    Rol.INGENIERO,
                ),
                u(
                    5,
                    "Sebastián Morales Gutiérrez",
                    "16.553.270-8",
                    "smorales@induztek.ejemplo",
                    Rol.INGENIERO,
                ),
                u(
                    6,
                    "Ricardo Espinoza Lagos",
                    "13.871.905-7",
                    "respinoza@induztek.ejemplo",
                    Rol.JEFE,
                ),
                u(
                    7,
                    "Marcela Fuentes Rojas",
                    "14.226.791-8",
                    "mfuentes@envpacifico.ejemplo",
                    Rol.CLIENTE,
                    1L,
                ),
                u(
                    8,
                    "Tomás Leiva Araya",
                    "18.390.452-3",
                    "tleiva@losaromos.ejemplo",
                    Rol.CLIENTE,
                    4L,
                ),
                u(
                    9,
                    "Patricio Navarro Soto",
                    "12.645.308-6",
                    "pnavarro@cerroalto.ejemplo",
                    Rol.CLIENTE,
                    3L,
                ),
            )
        val equipos =
            listOf(
                Equipo(
                    1L,
                    1L,
                    "TR-01",
                    Tipo.TRANSFORMADOR,
                    "Rhona",
                    "",
                    "RH-18733",
                    2012,
                    "1500 kVA; 12/0,4 kV",
                ),
                Equipo(
                    2L,
                    1L,
                    "TR-02",
                    Tipo.TRANSFORMADOR,
                    "Rhona",
                    "",
                    "RH-21904",
                    2017,
                    "1000 kVA; 12/0,4 kV",
                ),
                Equipo(
                    3L,
                    2L,
                    "TR-A",
                    Tipo.TRANSFORMADOR,
                    "Tusan",
                    "",
                    "TS-5521",
                    2009,
                    "2500 kVA; 23/0,4 kV",
                ),
                Equipo(
                    4L,
                    4L,
                    "CEL-MT-03",
                    Tipo.CELDA,
                    "Ormazabal",
                    "",
                    "OR-77812",
                    2018,
                    "12 kV; 630 A",
                    60.0,
                ),
                Equipo(
                    5L,
                    3L,
                    "INT-52-1",
                    Tipo.INTERRUPTOR,
                    "Schneider",
                    "",
                    "SE-449021",
                    2015,
                    "23 kV; 1250 A",
                    45.0,
                ),
                Equipo(
                    6L,
                    5L,
                    "REL-50/51-A",
                    Tipo.RELE,
                    "SEL",
                    "751",
                    "SEL-751-30219",
                    2020,
                    "Ajuste 5 A",
                ),
                Equipo(
                    7L,
                    6L,
                    "CAB-MT-02",
                    Tipo.CABLE,
                    "Nexans",
                    "",
                    "NX-2201-B",
                    2019,
                    "15 kV; 3×240 mm²; 180 m",
                ),
                Equipo(
                    8L,
                    3L,
                    "TR-SS-01",
                    Tipo.TRANSFORMADOR,
                    "ABB",
                    "",
                    "ABB-1180442",
                    2011,
                    "5000 kVA; 23/6,6 kV",
                ),
            )
        fun ins(i: Int, n: String, m: String, s: String, a: String, b: String) =
            Instrumento((i + 1).toLong(), n, "Megger", m, s, listOf(Calibracion(a, b)))
        val instrumentos =
            listOf(
                ins(0, "Megóhmetro 5 kV", "MIT525", "MIT-0045821", "2026-03-15", "2027-03-15"),
                ins(1, "Megóhmetro 10 kV", "MIT1025", "MIT-0071233", "2025-10-10", "2026-10-10"),
                ins(2, "Medidor TTR", "TTR300", "TTR-30044", "2026-06-02", "2027-06-02"),
                ins(3, "Micro-ohmímetro", "DLRO10X", "DLR-10882", "2025-08-20", "2026-08-20"),
                Instrumento(
                    5L,
                    "Inyección secundaria",
                    "Omicron",
                    "CMC356",
                    "CMC-HB411",
                    listOf(Calibracion("2026-01-05", "2027-01-05")),
                ),
            )
        val visitas =
            listOf("2024-09-14", "2025-09-13", "2026-09-26").mapIndexed { i, f ->
                Visita(
                    (i + 1).toLong(),
                    1L,
                    listOf("OT-2024-0312", "OT-2025-0288", "OT-2026-1187")[i],
                    f,
                    5L,
                    listOf(if (i == 1) 2L else 1L),
                    cierre = f + "T13:42:00",
                    recepcion =
                        Recepcion(
                            "Marcela Fuentes Rojas",
                            "14.226.791-8",
                            "Jefa de mantenimiento",
                            f + "T13:42:00",
                        ),
                )
            }
        val protocolos =
            listOf(12500.0 to 31250.0, 9800.0 to 21560.0, 6100.0 to 10980.0).mapIndexed { i, (a, b)
                ->
                val estado = if (i < 2) Estado.FORMALIZADO else Estado.REGISTRADO
                val fecha = visitas[i].fecha + "T09:40:00"
                Protocolo(
                    (i + 1).toLong(),
                    listOf("PT-2024-0312", "PT-2025-0288", "PT-2026-0401")[i],
                    (i + 1).toLong(),
                    1L,
                    if (i == 1) 2L else 1L,
                    fecha,
                    listOf(18.0, 21.0, 19.0)[i],
                    listOf(55.0, 48.0, 60.0)[i],
                    estado,
                    listOf(
                        Medicion(
                            id = (i + 1).toLong(),
                            prueba = Prueba.AISLACION,
                            elemento = "AT-T",
                            instrumentoId = 1L,
                            valores = mapOf("v" to 5.0, "r1" to a, "r10" to b),
                            resultado = if (i < 2) Resultado.APROBADO else Resultado.OBSERVADO,
                        )
                    ),
                    observaciones =
                        "Historial parcial proporcionado en Anexo 9; sin inventar otras mediciones.",
                    historial =
                        listOf(
                            Cambio(1L, Estado.BORRADOR, fecha),
                            Cambio(5L, estado, visitas[i].fecha + "T13:00:00"),
                        ),
                )
            }
        return Datos(usuarios, clientes, instalaciones, equipos, instrumentos, visitas, protocolos)
    }
}
