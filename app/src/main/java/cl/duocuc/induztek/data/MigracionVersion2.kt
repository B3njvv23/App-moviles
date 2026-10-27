package cl.duocuc.induztek.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.io.File

/** Conserva los datos anteriores y convierte IDs y referencias a claves numéricas. */
internal object MigracionVersion2 {
    fun migrar(db: SQLiteDatabase, crear: (SQLiteDatabase) -> Unit) {
        val tablas =
            listOf(
                "roles",
                "clientes",
                "usuarios",
                "instalaciones",
                "equipos",
                "instrumentos",
                "visitas",
                "protocolos",
                "calibraciones",
                "participantes",
                "recepciones",
                "lineas_acta",
                "mediciones",
                "valores_medicion",
                "historial",
                "meta",
            )
        val filas = tablas.associateWith { tabla ->
            db.rawQuery("SELECT * FROM $tabla ORDER BY rowid", null).use { c ->
                buildList {
                    while (c.moveToNext()) add(
                        c.columnNames.associateWith { nombre ->
                            val columna = c.getColumnIndexOrThrow(nombre)
                            if (c.isNull(columna)) null else c.getString(columna)
                        }
                    )
                }
            }
        }
        val entidades =
            listOf(
                "clientes",
                "usuarios",
                "instalaciones",
                "equipos",
                "instrumentos",
                "visitas",
                "protocolos",
                "mediciones",
            )
        val ids = entidades.associateWith { tabla ->
            filas.getValue(tabla).mapIndexed { i, fila -> fila.getValue("id") to (i + 1L) }.toMap()
        }
        val referencias =
            mapOf(
                "clienteId" to "clientes",
                "instalacionId" to "instalaciones",
                "equipoId" to "equipos",
                "instrumentoId" to "instrumentos",
                "visitaId" to "visitas",
                "protocoloId" to "protocolos",
                "creadorId" to "usuarios",
                "usuarioId" to "usuarios",
                "ingenieroId" to "usuarios",
                "reemplazaId" to "protocolos",
                "medicionId" to "mediciones",
            )
        listOf("equipo_tag", "protocolo_numero", "protocolo_equipo_visita", "visita_ot").forEach {
            db.execSQL("DROP INDEX IF EXISTS $it")
        }
        tablas.reversed().forEach { db.execSQL("DROP TABLE $it") }
        crear(db)
        tablas
            .filter { it != "roles" }
            .forEach { tabla ->
                filas.getValue(tabla).forEach { fila ->
                    val valores = ContentValues()
                    fila.forEach { (columna, valor) ->
                        when {
                            valor == null -> valores.putNull(columna)
                            columna == "id" && tabla in entidades ->
                                valores.put(columna, ids.getValue(tabla).getValue(valor))
                            columna in referencias -> {
                                val id = ids.getValue(referencias.getValue(columna))[valor]
                                if (id == null) valores.putNull(columna)
                                else valores.put(columna, id)
                            }
                            columna == "rol" && tabla == "usuarios" ->
                                valores.put(
                                    columna,
                                    cl.duocuc.induztek.model.Rol.valueOf(valor).ordinal + 1L,
                                )
                            columna == "evidencia" -> {
                                val foto =
                                    valor
                                        .takeIf { it.isNotBlank() }
                                        ?.let { runCatching { File(it).readBytes() }.getOrNull() }
                                if (foto == null) valores.putNull(columna)
                                else valores.put(columna, foto)
                            }
                            tabla == "meta" &&
                                columna == "clave" &&
                                valor.startsWith("recuperacion_") -> {
                                val nuevo =
                                    ids.getValue("usuarios")[valor.removePrefix("recuperacion_")]
                                valores.put(
                                    columna,
                                    if (nuevo == null) valor else "recuperacion_$nuevo",
                                )
                            }
                            else -> valores.put(columna, valor)
                        }
                    }
                    db.insertOrThrow(tabla, null, valores)
                }
            }
        ids.getValue("usuarios").forEach { (anterior, nuevo) ->
            db.insertOrThrow(
                "meta",
                null,
                ContentValues().apply {
                    put("clave", "usuario_anterior_$anterior")
                    put("valor", nuevo.toString())
                },
            )
        }
        db.rawQuery("PRAGMA foreign_key_check", null).use {
            check(!it.moveToFirst()) { "La migración dejó una referencia inválida" }
        }
    }
}
