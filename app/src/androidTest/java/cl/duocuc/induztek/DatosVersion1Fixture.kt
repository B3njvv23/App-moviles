package cl.duocuc.induztek

import cl.duocuc.induztek.model.*
import org.json.JSONArray
import org.json.JSONObject

/** Conversión explícita con la API JSON de Android, sin librerías externas. */
object DatosVersion1Fixture {
    private fun textos(j: JSONObject, campo: String): List<String> {
        val a = j.optJSONArray(campo) ?: JSONArray()
        return List(a.length()) { a.getString(it) }
    }

    private fun <T> objetos(j: JSONObject, campo: String, leer: (JSONObject) -> T): List<T> {
        val a = j.optJSONArray(campo) ?: JSONArray()
        return List(a.length()) { leer(a.getJSONObject(it)) }
    }

    fun codificar(obj: Any): JSONObject =
        when (obj) {
            is Usuario ->
                JSONObject()
                    .put("id", "u${obj.id}")
                    .put("nombre", obj.nombre)
                    .put("rut", obj.rut)
                    .put("correo", obj.correo)
                    .put("rol", obj.rol.name)
                    .put("clienteId", if (obj.clienteId == 0L) "" else "c${obj.clienteId}")
                    .put("activo", obj.activo)
                    .put("hash", obj.hash)
                    .put("sal", obj.sal)
            is Cliente ->
                JSONObject()
                    .put("id", "c${obj.id}")
                    .put("nombre", obj.nombre)
                    .put("rut", obj.rut)
                    .put("giro", obj.giro)
                    .put("contacto", obj.contacto)
                    .put("telefono", obj.telefono)
                    .put("correo", obj.correo)
            is Instalacion ->
                JSONObject()
                    .put("id", "i${obj.id}")
                    .put("clienteId", if (obj.clienteId == 0L) "" else "c${obj.clienteId}")
                    .put("nombre", obj.nombre)
                    .put("ubicacion", obj.ubicacion)
                    .put("comuna", obj.comuna)
                    .put("region", obj.region)
                    .put("acceso", obj.acceso)
            is Equipo ->
                JSONObject()
                    .put("id", "e${obj.id}")
                    .put(
                        "instalacionId",
                        if (obj.instalacionId == 0L) "" else "i${obj.instalacionId}",
                    )
                    .put("tag", obj.tag)
                    .put("tipo", obj.tipo.name)
                    .put("marca", obj.marca)
                    .put("modelo", obj.modelo)
                    .put("serie", obj.serie)
                    .put("anio", obj.anio)
                    .put("placa", obj.placa)
                    .put("maxContactos", obj.maxContactos)
                    .put("observaciones", obj.observaciones)
            is Calibracion -> JSONObject().put("desde", obj.desde).put("hasta", obj.hasta)
            is Instrumento ->
                JSONObject()
                    .put("id", "in${obj.id}")
                    .put("nombre", obj.nombre)
                    .put("marca", obj.marca)
                    .put("modelo", obj.modelo)
                    .put("serie", obj.serie)
                    .put("calibraciones", JSONArray(obj.calibraciones.map { codificar(it) }))
            is Recepcion ->
                JSONObject()
                    .put("nombre", obj.nombre)
                    .put("rut", obj.rut)
                    .put("cargo", obj.cargo)
                    .put("fecha", obj.fecha)
            is Visita ->
                JSONObject()
                    .put("id", "v${obj.id}")
                    .put(
                        "instalacionId",
                        if (obj.instalacionId == 0L) "" else "i${obj.instalacionId}",
                    )
                    .put("ot", obj.ot)
                    .put("fecha", obj.fecha)
                    .put("ingenieroId", if (obj.ingenieroId == 0L) "" else "u${obj.ingenieroId}")
                    .put("participantes", JSONArray(obj.participantes.map { "u$it" }))
                    .put("mantenimiento", obj.mantenimiento)
                    .put("cierre", obj.cierre)
                    .put("recepcion", obj.recepcion?.let { codificar(it) } ?: JSONObject.NULL)
                    .put("acta", JSONArray(obj.acta))
            is Cambio ->
                JSONObject()
                    .put("usuarioId", "u${obj.usuarioId}")
                    .put("estado", obj.estado.name)
                    .put("fecha", obj.fecha)
                    .put("motivo", obj.motivo)
            is Medicion ->
                JSONObject()
                    .put("id", "m${obj.id}")
                    .put("prueba", obj.prueba.name)
                    .put("elemento", obj.elemento)
                    .put("momento", obj.momento)
                    .put("realizada", obj.realizada)
                    .put("motivo", obj.motivo)
                    .put(
                        "instrumentoId",
                        if (obj.instrumentoId == 0L) "" else "in${obj.instrumentoId}",
                    )
                    .put("valores", JSONObject(obj.valores))
                    .put("resultado", obj.resultado?.name ?: JSONObject.NULL)
                    .put("comentario", obj.comentario)
            is Protocolo ->
                JSONObject()
                    .put("id", "p${obj.id}")
                    .put("numero", obj.numero)
                    .put("visitaId", if (obj.visitaId == 0L) "" else "v${obj.visitaId}")
                    .put("equipoId", if (obj.equipoId == 0L) "" else "e${obj.equipoId}")
                    .put("creadorId", if (obj.creadorId == 0L) "" else "u${obj.creadorId}")
                    .put("fecha", obj.fecha)
                    .put("temperatura", obj.temperatura)
                    .put("humedad", obj.humedad)
                    .put("estado", obj.estado.name)
                    .put("mediciones", JSONArray(obj.mediciones.map { codificar(it) }))
                    .put("mantenimiento", obj.mantenimiento)
                    .put("observaciones", obj.observaciones)
                    .put("historial", JSONArray(obj.historial.map { codificar(it) }))
                    .put("reemplazaId", if (obj.reemplazaId == 0L) "" else "p${obj.reemplazaId}")
                    .put("evidencia", "")
            else -> error("Tipo no soportado: ${obj.javaClass.simpleName}")
        }
}
