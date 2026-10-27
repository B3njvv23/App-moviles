package cl.duocuc.induztek.data

import android.database.sqlite.SQLiteDatabase
import cl.duocuc.induztek.model.*
import org.json.JSONArray
import org.json.JSONObject

/** Solo lee las bases de la versión anterior para migrarlas a tablas relacionales. */
internal object MigracionVersion1 {
    private val ids = linkedMapOf<String, Long>()

    private fun identificador(valor: String): Long =
        if (valor.isBlank()) 0L else ids.getOrPut(valor) { ids.size + 1L }

    fun metadatos(db: SQLiteDatabase) {
        db.rawQuery("SELECT clave,valor FROM meta_v1", null).use { c ->
            while (c.moveToNext()) {
                val clave = c.getString(0)
                val nueva =
                    if (clave.startsWith("recuperacion_"))
                        "recuperacion_${identificador(clave.removePrefix("recuperacion_"))}"
                    else clave
                db.insertOrThrow(
                    "meta",
                    null,
                    android.content.ContentValues().apply {
                        put("clave", nueva)
                        put("valor", c.getString(1))
                    },
                )
            }
        }
        ids.forEach { (anterior, nuevo) ->
            db.insertWithOnConflict(
                "meta",
                null,
                android.content.ContentValues().apply {
                    put("clave", "usuario_anterior_$anterior")
                    put("valor", nuevo.toString())
                },
                SQLiteDatabase.CONFLICT_REPLACE,
            )
        }
    }

    private fun textos(j: JSONObject, campo: String): List<String> {
        val a = j.optJSONArray(campo) ?: JSONArray()
        return List(a.length()) { a.getString(it) }
    }

    private fun <T> objetos(j: JSONObject, campo: String, leer: (JSONObject) -> T): List<T> {
        val a = j.optJSONArray(campo) ?: JSONArray()
        return List(a.length()) { leer(a.getJSONObject(it)) }
    }

    private fun leerUsuario(j: JSONObject) =
        Usuario(
            id = identificador(j.optString("id", "")),
            nombre = j.optString("nombre", ""),
            rut = j.optString("rut", ""),
            correo = j.optString("correo", ""),
            rol = Rol.valueOf(j.getString("rol")),
            clienteId = identificador(j.optString("clienteId", "")),
            activo = j.optBoolean("activo", true),
            hash = j.optString("hash", ""),
            sal = j.optString("sal", ""),
        )

    private fun leerCliente(j: JSONObject) =
        Cliente(
            id = identificador(j.optString("id", "")),
            nombre = j.optString("nombre", ""),
            rut = j.optString("rut", ""),
            giro = j.optString("giro", ""),
            contacto = j.optString("contacto", ""),
            telefono = j.optString("telefono", ""),
            correo = j.optString("correo", ""),
        )

    private fun leerInstalacion(j: JSONObject) =
        Instalacion(
            id = identificador(j.optString("id", "")),
            clienteId = identificador(j.optString("clienteId", "")),
            nombre = j.optString("nombre", ""),
            ubicacion = j.optString("ubicacion", ""),
            comuna = j.optString("comuna", ""),
            region = j.optString("region", ""),
            acceso = j.optString("acceso", ""),
        )

    private fun leerEquipo(j: JSONObject) =
        Equipo(
            id = identificador(j.optString("id", "")),
            instalacionId = identificador(j.optString("instalacionId", "")),
            tag = j.optString("tag", ""),
            tipo = Tipo.valueOf(j.getString("tipo")),
            marca = j.optString("marca", ""),
            modelo = j.optString("modelo", ""),
            serie = j.optString("serie", ""),
            anio = j.getInt("anio"),
            placa = j.optString("placa", ""),
            maxContactos = j.optDouble("maxContactos", 0.0),
            observaciones = j.optString("observaciones", ""),
        )

    private fun leerCalibracion(j: JSONObject) =
        Calibracion(
            desde = j.optString("desde", ""),
            hasta = j.optString("hasta", ""),
        )

    private fun leerInstrumento(j: JSONObject) =
        Instrumento(
            id = identificador(j.optString("id", "")),
            nombre = j.optString("nombre", ""),
            marca = j.optString("marca", ""),
            modelo = j.optString("modelo", ""),
            serie = j.optString("serie", ""),
            calibraciones = objetos(j, "calibraciones", ::leerCalibracion),
        )

    private fun leerRecepcion(j: JSONObject) =
        Recepcion(
            nombre = j.optString("nombre", ""),
            rut = j.optString("rut", ""),
            cargo = j.optString("cargo", ""),
            fecha = j.optString("fecha", ""),
        )

    private fun leerVisita(j: JSONObject) =
        Visita(
            id = identificador(j.optString("id", "")),
            instalacionId = identificador(j.optString("instalacionId", "")),
            ot = j.optString("ot", ""),
            fecha = j.optString("fecha", ""),
            ingenieroId = identificador(j.optString("ingenieroId", "")),
            participantes = textos(j, "participantes").map(::identificador),
            mantenimiento = j.optBoolean("mantenimiento", false),
            cierre = j.optString("cierre", ""),
            recepcion =
                if (j.isNull("recepcion")) null else leerRecepcion(j.getJSONObject("recepcion")),
            acta = textos(j, "acta"),
        )

    private fun leerCambio(j: JSONObject) =
        Cambio(
            usuarioId = identificador(j.optString("usuarioId", "")),
            estado = Estado.valueOf(j.getString("estado")),
            fecha = j.optString("fecha", ""),
            motivo = j.optString("motivo", ""),
        )

    private fun leerMedicion(j: JSONObject) =
        Medicion(
            id = identificador(j.optString("id", "")),
            prueba = Prueba.valueOf(j.getString("prueba")),
            elemento = j.optString("elemento", ""),
            momento = j.optString("momento", ""),
            realizada = j.optBoolean("realizada", true),
            motivo = j.optString("motivo", ""),
            instrumentoId = identificador(j.optString("instrumentoId", "")),
            valores =
                j.optJSONObject("valores")?.let { v ->
                    v.keys().asSequence().associateWith { v.getDouble(it) }
                } ?: emptyMap(),
            resultado =
                if (j.isNull("resultado")) null else Resultado.valueOf(j.getString("resultado")),
            comentario = j.optString("comentario", ""),
        )

    private fun leerProtocolo(j: JSONObject) =
        Protocolo(
            id = identificador(j.optString("id", "")),
            numero = j.optString("numero", ""),
            visitaId = identificador(j.optString("visitaId", "")),
            equipoId = identificador(j.optString("equipoId", "")),
            creadorId = identificador(j.optString("creadorId", "")),
            fecha = j.optString("fecha", ""),
            temperatura = j.optDouble("temperatura", 0.0),
            humedad = j.optDouble("humedad", 0.0),
            estado = Estado.valueOf(j.getString("estado")),
            mediciones = objetos(j, "mediciones", ::leerMedicion),
            mantenimiento = j.optString("mantenimiento", ""),
            observaciones = j.optString("observaciones", ""),
            historial = objetos(j, "historial", ::leerCambio),
            reemplazaId = identificador(j.optString("reemplazaId", "")),
            evidencia =
                j.optString("evidencia", "")
                    .takeIf { it.isNotBlank() }
                    ?.let { runCatching { java.io.File(it).readBytes() }.getOrNull() },
        )

    @Suppress("UNCHECKED_CAST")
    fun <T> leer(texto: String, tipo: Class<T>): T {
        val j = JSONObject(texto)
        return when (tipo) {
            Usuario::class.java -> leerUsuario(j)
            Cliente::class.java -> leerCliente(j)
            Instalacion::class.java -> leerInstalacion(j)
            Equipo::class.java -> leerEquipo(j)
            Calibracion::class.java -> leerCalibracion(j)
            Instrumento::class.java -> leerInstrumento(j)
            Recepcion::class.java -> leerRecepcion(j)
            Visita::class.java -> leerVisita(j)
            Cambio::class.java -> leerCambio(j)
            Medicion::class.java -> leerMedicion(j)
            Protocolo::class.java -> leerProtocolo(j)
            else -> error("Tipo no soportado: ${tipo.simpleName}")
        }
            as T
    }

    fun cargar(db: SQLiteDatabase): Datos {
        ids.clear()
        fun <T> lista(tabla: String, tipo: Class<T>): List<T> =
            db.rawQuery("SELECT json FROM $tabla ORDER BY rowid", null).use { c ->
                buildList { while (c.moveToNext()) add(leer(c.getString(0), tipo)) }
            }
        return Datos(
            lista("usuarios", Usuario::class.java),
            lista("clientes", Cliente::class.java),
            lista("instalaciones", Instalacion::class.java),
            lista("equipos", Equipo::class.java),
            lista("instrumentos", Instrumento::class.java),
            lista("visitas", Visita::class.java),
            lista("protocolos", Protocolo::class.java),
        )
    }
}
