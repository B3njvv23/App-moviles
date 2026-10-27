package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import cl.duocuc.induztek.viewmodel.Reglas

@Composable
fun VisitaScreen(
    ui: UiState,
    id: Long,
    ir: (String) -> Unit,
    form: AbrirFormulario,
    mensaje: (String) -> Unit,
) {
    val d = ui.datos
    val v = d.visitas.firstOrNull { it.id == id } ?: return
    val ps = d.protocolos.filter { it.visitaId == id }
    val instalacion = d.instalaciones.first { it.id == v.instalacionId }
    Contenido {
        Titulo(v.ot, "${instalacion.nombre} · ${v.fecha}")
        TarjetaSeccion("Resumen de visita") {
            Text(d.clientes.first { it.id == instalacion.clienteId }.nombre)
            Text("Ingeniero a cargo: ${d.usuarios.firstOrNull { it.id == v.ingenieroId }?.nombre}")
            Text(if (v.mantenimiento) "Pruebas y mantenimiento" else "Solo pruebas")
            Text(
                "${ps.size} equipos · ${ps.count { Reglas.global(it) == Resultado.OBSERVADO }} observados · ${ps.count { Reglas.global(it) == Resultado.RECHAZADO }} rechazados"
            )
            Etiqueta(if (v.cierre.isBlank()) "Abierta" else "Cerrada")
        }
        if (v.cierre.isBlank() && ui.usuario?.rol != Rol.CLIENTE) {
            BotonPrincipal(
                "Agregar protocolo",
                { form("protocolo", id.toString(), "") },
                Modifier.fillMaxWidth(),
            )
            if (ui.usuario?.rol in listOf(Rol.TECNICO, Rol.INGENIERO))
                BotonSecundario(
                    "Cerrar visita y registrar recepción",
                    { form("cerrar", id.toString(), "") },
                    Modifier.fillMaxWidth(),
                )
        }
        v.recepcion?.let { r ->
            TarjetaSeccion("Respaldo recibido") {
                Text(r.nombre)
                Text("${r.rut} · ${r.cargo}")
                Text(r.fecha)
                Text(
                    "La recepción registra la entrega, no constituye una firma electrónica avanzada.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (v.cierre.isNotBlank()) {
            TarjetaSeccion("Acta de prueba en terreno") {
                Text("Pendiente de formalización")
                Text(
                    "Entrega el respaldo al cliente mediante el menú Compartir o guárdalo para transferirlo sin conexión."
                )
                ExportarDocumento(d, visitaId = id, mensaje = mensaje)
            }
        }
        ps.forEach { p ->
            TarjetaProtocolo(p, d, ui.usuario?.rol == Rol.CLIENTE) { ir("protocolo/${p.id}") }
        }
        if (ps.isEmpty()) Vacio("Agrega un protocolo por cada equipo de esta visita.")
    }
}
