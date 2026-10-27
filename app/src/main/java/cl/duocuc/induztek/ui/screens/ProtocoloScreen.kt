package cl.duocuc.induztek.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import cl.duocuc.induztek.viewmodel.Reglas
import kotlin.math.abs

@Composable
fun ProtocoloScreen(ui: UiState, id: Long, form: AbrirFormulario, vm: ProtocolosViewModel) {
    val d = ui.datos
    val p = d.protocolos.firstOrNull { it.id == id } ?: return
    val e = d.equipos.first { it.id == p.equipoId }
    val u = ui.usuario ?: return
    val editable = Reglas.editable(u, p)
    val ingeniero = u.rol in listOf(Rol.INGENIERO, Rol.JEFE)
    val cambios = Reglas.significativas(p, d)
    Contenido {
        Titulo(e.tag, e.tipo.texto)
        Text(p.numero, style = MaterialTheme.typography.labelMedium)
        Etiqueta(
            if (u.rol == Rol.CLIENTE && p.estado != Estado.FORMALIZADO) "Pendiente de formalización"
            else p.estado.texto
        )
        TarjetaSeccion("Datos de la prueba") {
            Text("Fecha y hora: ${p.fecha}")
            Text("Responsable: ${d.usuarios.firstOrNull { it.id == p.creadorId }?.nombre}")
            Text("${p.temperatura} °C · ${p.humedad} % HR")
            Text("Resultado: ${Reglas.global(p)?.texto ?: "Pendiente de ingeniero"}")
            if (Reglas.vigentes(p).any { it.realizada && it.resultado == null })
                Text("El resultado global está pendiente de completar la revisión manual.")
            if (p.mantenimiento.isNotBlank()) Text("Mantenimiento: ${p.mantenimiento}")
            if (p.observaciones.isNotBlank()) Text(p.observaciones)
            if (editable)
                TextButton(onClick = { form("editar", id.toString(), "") }) { Text("Editar datos") }
            d.protocolos
                .firstOrNull { it.reemplazaId == id }
                ?.let {
                    Text("Reemplazado por nº ${it.numero}", color = MaterialTheme.colorScheme.error)
                }
            if (p.reemplazaId > 0L)
                Text(
                    "Reemplaza: ${d.protocolos.firstOrNull { it.id == p.reemplazaId }?.numero.orEmpty()}"
                )
        }
        if (cambios.isNotEmpty())
            TarjetaSeccion("Variación significativa > 25 %") {
                cambios.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                Text("El ingeniero debe comentar estos cambios antes de formalizar.")
            }
        if (editable) {
            val pendientes = Reglas.pendiente(p, e.tipo)
            TarjetaSeccion("Registro de pruebas") {
                Text(
                    "${pendientes.size} elementos por completar. Las pruebas no realizadas requieren motivo."
                )
                Reglas.pruebas(e.tipo).forEach { pr ->
                    Text(pr.texto, style = MaterialTheme.typography.titleSmall)
                    Reglas.elementos(e.tipo, pr).forEach { el ->
                        val lista = p.mediciones.filter { it.prueba == pr && it.elemento == el }
                        BotonSecundario(
                            "${if (lista.isEmpty()) "Registrar" else "Editar / repetir"} · $el",
                            { form("medicion", id.toString(), "${pr.name}|$el") },
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            EvidenciaFotografica(p, vm)
            BotonPrincipal(
                "Registrar en terreno",
                { vm.registrar(id) },
                Modifier.fillMaxWidth(),
                habilitado = !ui.ocupado && pendientes.isEmpty(),
                icono = R.drawable.ic_check,
            )
        } else if (p.evidencia != null && u.rol != Rol.CLIENTE) VistaEvidencia(p.evidencia)
        Titulo("Mediciones", "Se conservan las mediciones previas y posteriores al mantenimiento.")
        p.mediciones.forEach { m ->
            TarjetaSeccion(
                "${m.prueba.texto} · ${m.elemento} · ${m.momento}",
                Modifier.animateContentSize(),
            ) {
                if (!m.realizada) {
                    Etiqueta("No realizada")
                    Text(m.motivo)
                } else {
                    Reglas.parametros(e.tipo, m.prueba).forEach { spec ->
                        Text("${spec.etiqueta}: ${m.valores[spec.clave]}")
                    }
                    if (m.valores.containsKey("r10"))
                        Text(
                            "Índice de polarización: %.2f"
                                .format(m.valores.getValue("r10") / m.valores.getValue("r1"))
                        )
                    if (m.prueba == Prueba.TTR)
                        Text(
                            "Error: %.3f %%"
                                .format(
                                    abs(
                                        m.valores.getValue("medida") - m.valores.getValue("teorica")
                                    ) / m.valores.getValue("teorica") * 100
                                )
                        )
                    Etiqueta(
                        m.resultado?.texto ?: "Resultado pendiente de ingeniero",
                        m.resultado == Resultado.RECHAZADO,
                    )
                    Text(
                        "Instrumento: ${d.instrumentos.firstOrNull { it.id == m.instrumentoId }?.serie.orEmpty()}"
                    )
                    if (m.comentario.isNotBlank()) Text(m.comentario)
                    if (
                        ingeniero &&
                            p.estado !in listOf(Estado.FORMALIZADO, Estado.ANULADO) &&
                            Reglas.evaluar(e.tipo, m.prueba, m.valores, e.maxContactos) == null
                    ) {
                        Selector(
                            "Resultado manual",
                            m.resultado?.name.orEmpty(),
                            Resultado.entries.map { it.name to it.texto },
                            { vm.manual(id, m.id, Resultado.valueOf(it)) },
                        )
                    }
                }
            }
        }
        if (p.mediciones.isEmpty()) Vacio("Aún no se registran mediciones.")
        if (ingeniero && p.estado == Estado.REGISTRADO) {
            BotonPrincipal(
                "Formalizar protocolo",
                { form("estado", id.toString(), "FORMALIZADO") },
                Modifier.fillMaxWidth(),
            )
            BotonSecundario(
                "Devolver con observaciones",
                { form("estado", id.toString(), "OBSERVADO") },
                Modifier.fillMaxWidth(),
            )
        }
        if (ingeniero && p.estado !in listOf(Estado.FORMALIZADO, Estado.ANULADO))
            TextButton(onClick = { form("estado", id.toString(), "ANULADO") }) {
                Text("Anular con motivo")
            }
        if (d.visitas.any { it.id == p.visitaId && it.cierre.isNotBlank() })
            TarjetaSeccion("Documento") {
                ExportarDocumento(d, protocoloId = id, mensaje = vm::mensaje)
            }
        if (u.rol != Rol.CLIENTE)
            TarjetaSeccion("Trazabilidad") {
                p.historial.reversed().forEach { c ->
                    Text(
                        "${c.estado.texto} · ${d.usuarios.firstOrNull { it.id == c.usuarioId }?.nombre}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text("${c.fecha}\n${c.motivo}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                }
            }
    }
}
