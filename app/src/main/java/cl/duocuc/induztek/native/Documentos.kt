package cl.duocuc.induztek.native

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.viewmodel.Reglas
import java.io.File

/** Documento generado sin señal, guardado con SAF y compartido con permisos temporales. */
object Documentos {
    fun lineas(d: Datos, visitaId: Long = 0L, protocoloId: Long = 0L): List<String> {
        val v =
            if (visitaId > 0L) d.visitas.first { it.id == visitaId }
            else
                d.visitas.first {
                    it.id == d.protocolos.first { p -> p.id == protocoloId }.visitaId
                }
        if (visitaId > 0L && v.acta.isNotEmpty()) return v.acta
        val instalacion = d.instalaciones.first { it.id == v.instalacionId }
        val c = d.clientes.first { it.id == instalacion.clienteId }
        val protocolos =
            d.protocolos.filter {
                if (protocoloId > 0L) it.id == protocoloId
                else it.visitaId == v.id && it.estado != Estado.ANULADO
            }
        return buildList {
            add("INDUZTEK INGENIERÍA SpA")
            add(if (visitaId > 0L) "ACTA DE PRUEBA EN TERRENO" else "PROTOCOLO DE PRUEBA")
            add(
                if (visitaId > 0L || protocolos.any { it.estado != Estado.FORMALIZADO })
                    "PENDIENTE DE FORMALIZACIÓN"
                else "FORMALIZADO"
            )
            add("Datos ficticios - MVP académico DSY1105")
            add("Cliente: ${c.nombre} - RUT: ${c.rut}")
            add("Instalación: ${instalacion.nombre} - ${instalacion.ubicacion}")
            add("OT: ${v.ot} - Fecha: ${v.fecha}")
            add(
                "Ingeniero a cargo: ${d.usuarios.firstOrNull { it.id == v.ingenieroId }?.nombre.orEmpty()}"
            )
            add("Visita: ${if (v.mantenimiento) "Pruebas y mantenimiento" else "Solo pruebas"}")
            protocolos.forEach { p ->
                val e = d.equipos.first { it.id == p.equipoId }
                add("")
                add("PROTOCOLO ${p.numero}")
                add("${e.tag} - ${e.tipo.texto} - ${e.marca} - Serie: ${e.serie}")
                add("Placa: ${e.placa}")
                add(
                    "Técnico: ${d.usuarios.firstOrNull { it.id == p.creadorId }?.nombre} - Prueba: ${p.fecha}"
                )
                add("Ambiente: ${p.temperatura} °C - ${p.humedad} % HR")
                add(
                    "Resultado global: ${Reglas.global(p)?.texto ?: "Pendiente de revisión manual"}"
                )
                if (p.mantenimiento.isNotBlank()) add("Mantenimiento: ${p.mantenimiento}")
                p.mediciones.forEach { m ->
                    add("${m.prueba.texto} / ${m.elemento} / ${m.momento}")
                    if (!m.realizada) add("NO REALIZADA: ${m.motivo}")
                    else {
                        Reglas.parametros(e.tipo, m.prueba).forEach {
                            add("  ${it.etiqueta}: ${m.valores[it.clave]}")
                        }
                        if (m.valores.containsKey("r10"))
                            add(
                                "  IP: %.2f"
                                    .format(m.valores.getValue("r10") / m.valores.getValue("r1"))
                            )
                        add("  Resultado: ${m.resultado?.texto ?: "Pendiente de ingeniero"}")
                        val instrumento = d.instrumentos.firstOrNull { it.id == m.instrumentoId }
                        add("  Instrumento: ${instrumento?.nombre} - Serie: ${instrumento?.serie}")
                        instrumento
                            ?.calibraciones
                            ?.firstOrNull {
                                p.fecha.take(10) >= it.desde && p.fecha.take(10) <= it.hasta
                            }
                            ?.let { add("  Calibración: ${it.desde} a ${it.hasta}") }
                        if (m.comentario.isNotBlank()) add("  Comentario: ${m.comentario}")
                    }
                }
                Reglas.significativas(p, d).forEach { add("VARIACIÓN SIGNIFICATIVA: $it") }
                if (p.observaciones.isNotBlank()) add("Observaciones: ${p.observaciones}")
                p.historial
                    .lastOrNull { it.estado == Estado.FORMALIZADO }
                    ?.let {
                        add(
                            "Formalizó: ${d.usuarios.firstOrNull { u -> u.id == it.usuarioId }?.nombre} - ${it.fecha} - ${it.motivo}"
                        )
                    }
                d.protocolos
                    .firstOrNull { it.reemplazaId == p.id }
                    ?.let { add("Reemplazado por nº ${it.numero}") }
            }
            add("")
            add("RECEPCIÓN DEL RESPALDO")
            v.recepcion?.let {
                add("Recibe: ${it.nombre} - RUT: ${it.rut}")
                add("Cargo: ${it.cargo} - Fecha y hora: ${it.fecha}")
            } ?: add("Recepción aún no registrada")
        }
    }

    fun crear(context: Context, lineas: List<String>): File {
        val dir = File(context.cacheDir, "actas").apply { mkdirs() }
        val file = File(dir, "Induztek-${System.currentTimeMillis()}.pdf")
        val pdf = PdfDocument()
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 10f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
        val rows = mutableListOf<String>()
        lineas.forEach { line ->
            var actual = ""
            // También parte códigos UUID y palabras largas para no desbordar el margen.
            line.forEach { ch ->
                if (paint.measureText(actual + ch) > 505f) {
                    rows.add(actual)
                    actual = ""
                }
                actual += ch
            }
            rows.add(actual)
        }
        rows.chunked(45).forEachIndexed { i, part ->
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, i + 1).create())
            paint.color = android.graphics.Color.rgb(35, 28, 67)
            part.forEachIndexed { j, line -> page.canvas.drawText(line, 40f, 50f + j * 16, paint) }
            paint.color = android.graphics.Color.GRAY
            page.canvas.drawText(
                "Induztek - Página ${i + 1} de ${(rows.size + 44) / 45}",
                40f,
                812f,
                paint,
            )
            pdf.finishPage(page)
        }
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    fun compartir(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Entregar respaldo",
            )
        )
    }

    fun copiar(context: Context, file: File, uri: Uri) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            file.inputStream().use { it.copyTo(out) }
        } ?: error("No se pudo abrir el destino")
    }
}
