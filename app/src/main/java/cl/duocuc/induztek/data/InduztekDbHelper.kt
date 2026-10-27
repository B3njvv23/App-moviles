package cl.duocuc.induztek.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.induztek.model.*

/** SQLite del sistema: tablas, columnas, ContentValues y consultas con Cursor. */
class InduztekDbHelper(context: Context, nombre: String = "induztek.db") :
    SQLiteOpenHelper(context, nombre, null, VERSION_BD) {

    companion object {
        private const val VERSION_BD = 3
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE roles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                rut TEXT NOT NULL,
                correo TEXT NOT NULL,
                rol INTEGER NOT NULL REFERENCES roles(id) DEFERRABLE INITIALLY DEFERRED,
                clienteId INTEGER REFERENCES clientes(id) DEFERRABLE INITIALLY DEFERRED,
                activo INTEGER NOT NULL CHECK(activo IN (0,1)),
                hash TEXT NOT NULL,
                sal TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                rut TEXT NOT NULL,
                giro TEXT NOT NULL,
                contacto TEXT NOT NULL,
                telefono TEXT NOT NULL,
                correo TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE instalaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                clienteId INTEGER NOT NULL REFERENCES clientes(id) DEFERRABLE INITIALLY DEFERRED,
                nombre TEXT NOT NULL,
                ubicacion TEXT NOT NULL,
                comuna TEXT NOT NULL,
                region TEXT NOT NULL,
                acceso TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE equipos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                instalacionId INTEGER NOT NULL REFERENCES instalaciones(id) DEFERRABLE INITIALLY DEFERRED,
                tag TEXT NOT NULL,
                tipo TEXT NOT NULL,
                marca TEXT NOT NULL,
                modelo TEXT NOT NULL,
                serie TEXT NOT NULL,
                anio INTEGER NOT NULL,
                placa TEXT NOT NULL,
                maxContactos REAL NOT NULL,
                observaciones TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE instrumentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                marca TEXT NOT NULL,
                modelo TEXT NOT NULL,
                serie TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE visitas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                instalacionId INTEGER NOT NULL REFERENCES instalaciones(id) DEFERRABLE INITIALLY DEFERRED,
                ot TEXT NOT NULL,
                fecha TEXT NOT NULL,
                ingenieroId INTEGER NOT NULL REFERENCES usuarios(id) DEFERRABLE INITIALLY DEFERRED,
                mantenimiento INTEGER NOT NULL CHECK(mantenimiento IN (0,1)),
                cierre TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE protocolos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT NOT NULL,
                visitaId INTEGER NOT NULL REFERENCES visitas(id) DEFERRABLE INITIALLY DEFERRED,
                equipoId INTEGER NOT NULL REFERENCES equipos(id) DEFERRABLE INITIALLY DEFERRED,
                creadorId INTEGER NOT NULL REFERENCES usuarios(id) DEFERRABLE INITIALLY DEFERRED,
                fecha TEXT NOT NULL,
                temperatura REAL NOT NULL,
                humedad REAL NOT NULL,
                estado TEXT NOT NULL,
                mantenimiento TEXT NOT NULL,
                observaciones TEXT NOT NULL,
                reemplazaId INTEGER REFERENCES protocolos(id) DEFERRABLE INITIALLY DEFERRED,
                evidencia BLOB
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE calibraciones (
                instrumentoId INTEGER NOT NULL REFERENCES instrumentos(id),
                orden INTEGER NOT NULL,
                desde TEXT NOT NULL,
                hasta TEXT NOT NULL,
                PRIMARY KEY(instrumentoId,orden)
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE participantes (
                visitaId INTEGER NOT NULL REFERENCES visitas(id),
                orden INTEGER NOT NULL,
                usuarioId INTEGER NOT NULL REFERENCES usuarios(id),
                PRIMARY KEY(visitaId,orden)
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE recepciones (
                visitaId INTEGER PRIMARY KEY REFERENCES visitas(id),
                nombre TEXT NOT NULL,
                rut TEXT NOT NULL,
                cargo TEXT NOT NULL,
                fecha TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE lineas_acta (
                visitaId INTEGER NOT NULL REFERENCES visitas(id),
                orden INTEGER NOT NULL,
                texto TEXT NOT NULL,
                PRIMARY KEY(visitaId,orden)
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE mediciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                protocoloId INTEGER NOT NULL REFERENCES protocolos(id),
                orden INTEGER NOT NULL,
                prueba TEXT NOT NULL,
                elemento TEXT NOT NULL,
                momento TEXT NOT NULL,
                realizada INTEGER NOT NULL CHECK(realizada IN (0,1)),
                motivo TEXT NOT NULL,
                instrumentoId INTEGER REFERENCES instrumentos(id),
                resultado TEXT,
                comentario TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE valores_medicion (
                medicionId INTEGER NOT NULL REFERENCES mediciones(id),
                clave TEXT NOT NULL,
                valor REAL NOT NULL,
                PRIMARY KEY(medicionId,clave)
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE historial (
                protocoloId INTEGER NOT NULL REFERENCES protocolos(id),
                orden INTEGER NOT NULL,
                usuarioId INTEGER NOT NULL REFERENCES usuarios(id),
                estado TEXT NOT NULL,
                fecha TEXT NOT NULL,
                motivo TEXT NOT NULL,
                PRIMARY KEY(protocoloId,orden)
            )
            """
                .trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE meta (
                clave TEXT PRIMARY KEY,
                valor TEXT NOT NULL
            )
            """
                .trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX equipo_tag ON equipos(instalacionId,tag COLLATE NOCASE)")
        db.execSQL("CREATE UNIQUE INDEX protocolo_numero ON protocolos(numero)")
        db.execSQL("CREATE UNIQUE INDEX protocolo_equipo_visita ON protocolos(visitaId,equipoId)")
        db.execSQL("CREATE UNIQUE INDEX visita_ot ON visitas(ot COLLATE NOCASE)")
        Rol.entries.forEach { rol ->
            db.insertOrThrow(
                "roles",
                null,
                ContentValues().apply {
                    put("id", rol.ordinal + 1L)
                    put("codigo", rol.name)
                    put("nombre", rol.texto)
                },
            )
        }
    }

    private fun Cursor.texto(columna: String) = getString(getColumnIndexOrThrow(columna)).orEmpty()

    private fun Cursor.identificador(columna: String) = getLong(getColumnIndexOrThrow(columna))

    private fun Cursor.entero(columna: String) = getInt(getColumnIndexOrThrow(columna))

    private fun Cursor.numero(columna: String) = getDouble(getColumnIndexOrThrow(columna))

    private fun <T> consultar(
        db: SQLiteDatabase,
        sql: String,
        args: Array<String>? = null,
        convertir: (Cursor) -> T,
    ): List<T> =
        db.rawQuery(sql, args).use { cursor ->
            buildList { while (cursor.moveToNext()) add(convertir(cursor)) }
        }

    private fun valoresDeUsuario(obj: Usuario) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("nombre", obj.nombre)
            put("rut", obj.rut)
            put("correo", obj.correo)
            put("rol", obj.rol.ordinal + 1L)
            put("clienteId", obj.clienteId.takeIf { it > 0L })
            put("activo", if (obj.activo) 1 else 0)
            put("hash", obj.hash)
            put("sal", obj.sal)
        }

    fun listarUsuarios(): List<Usuario> =
        consultar(
            readableDatabase,
            "SELECT u.*, r.codigo AS codigoRol FROM usuarios u INNER JOIN roles r ON u.rol=r.id ORDER BY u.rowid",
        ) { c ->
            Usuario(
                id = c.identificador("id"),
                nombre = c.texto("nombre"),
                rut = c.texto("rut"),
                correo = c.texto("correo"),
                rol = Rol.valueOf(c.texto("codigoRol")),
                clienteId = c.identificador("clienteId"),
                activo = c.entero("activo") == 1,
                hash = c.texto("hash"),
                sal = c.texto("sal"),
            )
        }

    private fun valoresDeCliente(obj: Cliente) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("nombre", obj.nombre)
            put("rut", obj.rut)
            put("giro", obj.giro)
            put("contacto", obj.contacto)
            put("telefono", obj.telefono)
            put("correo", obj.correo)
        }

    fun listarClientes(): List<Cliente> =
        consultar(readableDatabase, "SELECT * FROM clientes ORDER BY rowid") { c ->
            Cliente(
                id = c.identificador("id"),
                nombre = c.texto("nombre"),
                rut = c.texto("rut"),
                giro = c.texto("giro"),
                contacto = c.texto("contacto"),
                telefono = c.texto("telefono"),
                correo = c.texto("correo"),
            )
        }

    private fun valoresDeInstalacion(obj: Instalacion) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("clienteId", obj.clienteId)
            put("nombre", obj.nombre)
            put("ubicacion", obj.ubicacion)
            put("comuna", obj.comuna)
            put("region", obj.region)
            put("acceso", obj.acceso)
        }

    fun listarInstalaciones(): List<Instalacion> =
        consultar(readableDatabase, "SELECT * FROM instalaciones ORDER BY rowid") { c ->
            Instalacion(
                id = c.identificador("id"),
                clienteId = c.identificador("clienteId"),
                nombre = c.texto("nombre"),
                ubicacion = c.texto("ubicacion"),
                comuna = c.texto("comuna"),
                region = c.texto("region"),
                acceso = c.texto("acceso"),
            )
        }

    private fun valoresDeEquipo(obj: Equipo) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("instalacionId", obj.instalacionId)
            put("tag", obj.tag)
            put("tipo", obj.tipo.name)
            put("marca", obj.marca)
            put("modelo", obj.modelo)
            put("serie", obj.serie)
            put("anio", obj.anio)
            put("placa", obj.placa)
            put("maxContactos", obj.maxContactos)
            put("observaciones", obj.observaciones)
        }

    fun listarEquipos(): List<Equipo> =
        consultar(readableDatabase, "SELECT * FROM equipos ORDER BY rowid") { c ->
            Equipo(
                id = c.identificador("id"),
                instalacionId = c.identificador("instalacionId"),
                tag = c.texto("tag"),
                tipo = Tipo.valueOf(c.texto("tipo")),
                marca = c.texto("marca"),
                modelo = c.texto("modelo"),
                serie = c.texto("serie"),
                anio = c.entero("anio"),
                placa = c.texto("placa"),
                maxContactos = c.numero("maxContactos"),
                observaciones = c.texto("observaciones"),
            )
        }

    private fun valoresDeInstrumento(obj: Instrumento) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("nombre", obj.nombre)
            put("marca", obj.marca)
            put("modelo", obj.modelo)
            put("serie", obj.serie)
        }

    fun listarInstrumentos(): List<Instrumento> =
        consultar(readableDatabase, "SELECT * FROM instrumentos ORDER BY rowid") { c ->
            Instrumento(
                id = c.identificador("id"),
                nombre = c.texto("nombre"),
                marca = c.texto("marca"),
                modelo = c.texto("modelo"),
                serie = c.texto("serie"),
                calibraciones = listarCalibraciones(c.identificador("id")),
            )
        }

    private fun valoresDeVisita(obj: Visita) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("instalacionId", obj.instalacionId)
            put("ot", obj.ot)
            put("fecha", obj.fecha)
            put("ingenieroId", obj.ingenieroId)
            put("mantenimiento", if (obj.mantenimiento) 1 else 0)
            put("cierre", obj.cierre)
        }

    fun listarVisitas(): List<Visita> =
        consultar(readableDatabase, "SELECT * FROM visitas ORDER BY rowid") { c ->
            Visita(
                id = c.identificador("id"),
                instalacionId = c.identificador("instalacionId"),
                ot = c.texto("ot"),
                fecha = c.texto("fecha"),
                ingenieroId = c.identificador("ingenieroId"),
                mantenimiento = c.entero("mantenimiento") == 1,
                cierre = c.texto("cierre"),
                participantes = listarParticipantes(c.identificador("id")),
                recepcion = buscarRecepcion(c.identificador("id")),
                acta = listarActa(c.identificador("id")),
            )
        }

    private fun valoresDeProtocolo(obj: Protocolo) =
        ContentValues().apply {
            if (obj.id > 0L) put("id", obj.id)
            put("numero", obj.numero)
            put("visitaId", obj.visitaId)
            put("equipoId", obj.equipoId)
            put("creadorId", obj.creadorId)
            put("fecha", obj.fecha)
            put("temperatura", obj.temperatura)
            put("humedad", obj.humedad)
            put("estado", obj.estado.name)
            put("mantenimiento", obj.mantenimiento)
            put("observaciones", obj.observaciones)
            put("reemplazaId", obj.reemplazaId.takeIf { it > 0L })
            put("evidencia", obj.evidencia)
        }

    fun listarProtocolos(): List<Protocolo> =
        consultar(readableDatabase, "SELECT * FROM protocolos ORDER BY rowid") { c ->
            Protocolo(
                id = c.identificador("id"),
                numero = c.texto("numero"),
                visitaId = c.identificador("visitaId"),
                equipoId = c.identificador("equipoId"),
                creadorId = c.identificador("creadorId"),
                fecha = c.texto("fecha"),
                temperatura = c.numero("temperatura"),
                humedad = c.numero("humedad"),
                estado = Estado.valueOf(c.texto("estado")),
                mantenimiento = c.texto("mantenimiento"),
                observaciones = c.texto("observaciones"),
                reemplazaId = c.identificador("reemplazaId"),
                evidencia =
                    c.getColumnIndexOrThrow("evidencia").let {
                        if (c.isNull(it)) null else c.getBlob(it)
                    },
                mediciones = listarMediciones(c.identificador("id")),
                historial = listarHistorial(c.identificador("id")),
            )
        }

    private fun listarCalibraciones(id: Long) =
        consultar(
            readableDatabase,
            "SELECT * FROM calibraciones WHERE instrumentoId=? ORDER BY orden",
            arrayOf(id.toString()),
        ) { c ->
            Calibracion(c.texto("desde"), c.texto("hasta"))
        }

    private fun listarParticipantes(id: Long) =
        consultar(
            readableDatabase,
            "SELECT usuarioId FROM participantes WHERE visitaId=? ORDER BY orden",
            arrayOf(id.toString()),
        ) { c ->
            c.identificador("usuarioId")
        }

    private fun buscarRecepcion(id: Long) =
        consultar(
                readableDatabase,
                "SELECT * FROM recepciones WHERE visitaId=?",
                arrayOf(id.toString()),
            ) { c ->
                Recepcion(c.texto("nombre"), c.texto("rut"), c.texto("cargo"), c.texto("fecha"))
            }
            .firstOrNull()

    private fun listarActa(id: Long) =
        consultar(
            readableDatabase,
            "SELECT texto FROM lineas_acta WHERE visitaId=? ORDER BY orden",
            arrayOf(id.toString()),
        ) { c ->
            c.texto("texto")
        }

    private fun listarHistorial(id: Long) =
        consultar(
            readableDatabase,
            "SELECT * FROM historial WHERE protocoloId=? ORDER BY orden",
            arrayOf(id.toString()),
        ) { c ->
            Cambio(
                c.identificador("usuarioId"),
                Estado.valueOf(c.texto("estado")),
                c.texto("fecha"),
                c.texto("motivo"),
            )
        }

    private fun listarMediciones(id: Long): List<Medicion> =
        consultar(
            readableDatabase,
            "SELECT * FROM mediciones WHERE protocoloId=? ORDER BY orden",
            arrayOf(id.toString()),
        ) { c ->
            val valores =
                consultar(
                        readableDatabase,
                        "SELECT clave,valor FROM valores_medicion WHERE medicionId=? ORDER BY rowid",
                        arrayOf(c.identificador("id").toString()),
                    ) { v ->
                        v.texto("clave") to v.numero("valor")
                    }
                    .toMap()
            Medicion(
                id = c.identificador("id"),
                prueba = Prueba.valueOf(c.texto("prueba")),
                elemento = c.texto("elemento"),
                momento = c.texto("momento"),
                realizada = c.entero("realizada") == 1,
                motivo = c.texto("motivo"),
                instrumentoId = c.identificador("instrumentoId"),
                valores = valores,
                resultado =
                    c.texto("resultado").takeIf { it.isNotBlank() }?.let { Resultado.valueOf(it) },
                comentario = c.texto("comentario"),
            )
        }

    fun cargar(): Datos? {
        val usuarios = listarUsuarios()
        return if (usuarios.isEmpty()) null
        else
            Datos(
                usuarios,
                listarClientes(),
                listarInstalaciones(),
                listarEquipos(),
                listarInstrumentos(),
                listarVisitas(),
                listarProtocolos(),
            )
    }

    fun insertarCliente(obj: Cliente): Long =
        writableDatabase.insertOrThrow("clientes", null, valoresDeCliente(obj))

    fun actualizarCliente(obj: Cliente): Int =
        writableDatabase.update(
            "clientes",
            valoresDeCliente(obj),
            "id=?",
            arrayOf(obj.id.toString()),
        )

    fun insertarUsuario(obj: Usuario): Long =
        writableDatabase.insertOrThrow("usuarios", null, valoresDeUsuario(obj))

    fun actualizarUsuario(obj: Usuario): Int =
        writableDatabase.update(
            "usuarios",
            valoresDeUsuario(obj),
            "id=?",
            arrayOf(obj.id.toString()),
        )

    fun insertarInstalacion(obj: Instalacion): Long =
        writableDatabase.insertOrThrow("instalaciones", null, valoresDeInstalacion(obj))

    fun actualizarInstalacion(obj: Instalacion): Int =
        writableDatabase.update(
            "instalaciones",
            valoresDeInstalacion(obj),
            "id=?",
            arrayOf(obj.id.toString()),
        )

    fun insertarEquipo(obj: Equipo): Long =
        writableDatabase.insertOrThrow("equipos", null, valoresDeEquipo(obj))

    fun actualizarEquipo(obj: Equipo): Int =
        writableDatabase.update("equipos", valoresDeEquipo(obj), "id=?", arrayOf(obj.id.toString()))

    fun insertarInstrumento(obj: Instrumento): Long = transaccion { db ->
        val id = db.insertOrThrow("instrumentos", null, valoresDeInstrumento(obj))
        detallesInstrumento(db, obj.copy(id = id))
        id
    }

    fun actualizarInstrumento(obj: Instrumento): Int = transaccion { db ->
        val filas =
            db.update("instrumentos", valoresDeInstrumento(obj), "id=?", arrayOf(obj.id.toString()))
        require(filas == 1) { "El registro no existe" }
        detallesInstrumento(db, obj)
        filas
    }

    fun insertarVisita(obj: Visita): Long = transaccion { db ->
        val id = db.insertOrThrow("visitas", null, valoresDeVisita(obj))
        detallesVisita(db, obj.copy(id = id))
        id
    }

    fun actualizarVisita(obj: Visita): Int = transaccion { db ->
        val filas = db.update("visitas", valoresDeVisita(obj), "id=?", arrayOf(obj.id.toString()))
        require(filas == 1) { "El registro no existe" }
        detallesVisita(db, obj)
        filas
    }

    fun insertarProtocolo(obj: Protocolo): Long = transaccion { db ->
        val id = db.insertOrThrow("protocolos", null, valoresDeProtocolo(obj))
        detallesProtocolo(db, obj.copy(id = id))
        id
    }

    fun actualizarProtocolo(obj: Protocolo): Int = transaccion { db ->
        val filas =
            db.update("protocolos", valoresDeProtocolo(obj), "id=?", arrayOf(obj.id.toString()))
        require(filas == 1) { "El registro no existe" }
        detallesProtocolo(db, obj)
        filas
    }

    private fun detallesInstrumento(db: SQLiteDatabase, instrumento: Instrumento) {
        db.delete("calibraciones", "instrumentoId=?", arrayOf(instrumento.id.toString()))
        instrumento.calibraciones.forEachIndexed { orden, c ->
            db.insertOrThrow(
                "calibraciones",
                null,
                ContentValues().apply {
                    put("instrumentoId", instrumento.id)
                    put("orden", orden)
                    put("desde", c.desde)
                    put("hasta", c.hasta)
                },
            )
        }
    }

    private fun detallesVisita(db: SQLiteDatabase, visita: Visita) {
        db.delete("participantes", "visitaId=?", arrayOf(visita.id.toString()))
        db.delete("recepciones", "visitaId=?", arrayOf(visita.id.toString()))
        db.delete("lineas_acta", "visitaId=?", arrayOf(visita.id.toString()))
        visita.participantes.forEachIndexed { orden, usuario ->
            db.insertOrThrow(
                "participantes",
                null,
                ContentValues().apply {
                    put("visitaId", visita.id)
                    put("orden", orden)
                    put("usuarioId", usuario)
                },
            )
        }
        visita.recepcion?.let { r ->
            db.insertOrThrow(
                "recepciones",
                null,
                ContentValues().apply {
                    put("visitaId", visita.id)
                    put("nombre", r.nombre)
                    put("rut", r.rut)
                    put("cargo", r.cargo)
                    put("fecha", r.fecha)
                },
            )
        }
        visita.acta.forEachIndexed { orden, texto ->
            db.insertOrThrow(
                "lineas_acta",
                null,
                ContentValues().apply {
                    put("visitaId", visita.id)
                    put("orden", orden)
                    put("texto", texto)
                },
            )
        }
    }

    private fun detallesProtocolo(db: SQLiteDatabase, protocolo: Protocolo) {
        db.delete(
            "valores_medicion",
            "medicionId IN (SELECT id FROM mediciones WHERE protocoloId=?)",
            arrayOf(protocolo.id.toString()),
        )
        db.delete("mediciones", "protocoloId=?", arrayOf(protocolo.id.toString()))
        db.delete("historial", "protocoloId=?", arrayOf(protocolo.id.toString()))
        protocolo.mediciones.forEachIndexed { orden, m ->
            val medicionId =
                db.insertOrThrow(
                    "mediciones",
                    null,
                    ContentValues().apply {
                        if (m.id > 0L) put("id", m.id)
                        put("protocoloId", protocolo.id)
                        put("orden", orden)
                        put("prueba", m.prueba.name)
                        put("elemento", m.elemento)
                        put("momento", m.momento)
                        put("realizada", if (m.realizada) 1 else 0)
                        put("motivo", m.motivo)
                        put("instrumentoId", m.instrumentoId.takeIf { it > 0L })
                        put("resultado", m.resultado?.name)
                        put("comentario", m.comentario)
                    },
                )
            m.valores.forEach { (clave, valor) ->
                db.insertOrThrow(
                    "valores_medicion",
                    null,
                    ContentValues().apply {
                        put("medicionId", medicionId)
                        put("clave", clave)
                        put("valor", valor)
                    },
                )
            }
        }
        protocolo.historial.forEachIndexed { orden, h ->
            db.insertOrThrow(
                "historial",
                null,
                ContentValues().apply {
                    put("protocoloId", protocolo.id)
                    put("orden", orden)
                    put("usuarioId", h.usuarioId)
                    put("estado", h.estado.name)
                    put("fecha", h.fecha)
                    put("motivo", h.motivo)
                },
            )
        }
    }

    private fun <T> transaccion(operacion: (SQLiteDatabase) -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val resultado = operacion(db)
            db.setTransactionSuccessful()
            return resultado
        } finally {
            db.endTransaction()
        }
    }

    fun precargar(datos: Datos) = transaccion {
        datos.clientes.forEach { insertarCliente(it) }
        datos.usuarios.forEach { insertarUsuario(it) }
        datos.instalaciones.forEach { insertarInstalacion(it) }
        datos.equipos.forEach { insertarEquipo(it) }
        datos.instrumentos.forEach { insertarInstrumento(it) }
        datos.visitas.forEach { insertarVisita(it) }
        datos.protocolos.forEach { insertarProtocolo(it) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 2) MigracionVersion2.migrar(db, ::onCreate)
        else if (oldVersion == 1) {
            val datos = MigracionVersion1.cargar(db)
            listOf("equipo_tag", "protocolo_numero", "protocolo_equipo_visita", "visita_ot")
                .forEach { db.execSQL("DROP INDEX IF EXISTS $it") }
            listOf(
                    "protocolos",
                    "visitas",
                    "instrumentos",
                    "equipos",
                    "instalaciones",
                    "usuarios",
                    "clientes",
                )
                .forEach { db.execSQL("DROP TABLE $it") }
            // La tabla meta se conserva y se vuelve a crear junto con el esquema nuevo.
            db.execSQL("ALTER TABLE meta RENAME TO meta_v1")
            onCreate(db)
            // onUpgrade ya dispone de una base abierta; no se vuelve a abrir el helper.
            datos.clientes.forEach { db.insertOrThrow("clientes", null, valoresDeCliente(it)) }
            datos.usuarios.forEach { db.insertOrThrow("usuarios", null, valoresDeUsuario(it)) }
            datos.instalaciones.forEach {
                db.insertOrThrow("instalaciones", null, valoresDeInstalacion(it))
            }
            datos.equipos.forEach { db.insertOrThrow("equipos", null, valoresDeEquipo(it)) }
            datos.instrumentos.forEach {
                db.insertOrThrow("instrumentos", null, valoresDeInstrumento(it))
                detallesInstrumento(db, it)
            }
            datos.visitas.forEach {
                db.insertOrThrow("visitas", null, valoresDeVisita(it))
                detallesVisita(db, it)
            }
            datos.protocolos.forEach {
                db.insertOrThrow("protocolos", null, valoresDeProtocolo(it))
                detallesProtocolo(db, it)
            }
            MigracionVersion1.metadatos(db)
            db.execSQL("DROP TABLE meta_v1")
        } else error("Migración no definida de $oldVersion a $newVersion")
    }

    fun meta(clave: String): String? =
        consultar(readableDatabase, "SELECT valor FROM meta WHERE clave=?", arrayOf(clave)) {
                it.texto("valor")
            }
            .firstOrNull()

    fun meta(clave: String, valor: String) {
        writableDatabase.insertWithOnConflict(
            "meta",
            null,
            ContentValues().apply {
                put("clave", clave)
                put("valor", valor)
            },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }
}
