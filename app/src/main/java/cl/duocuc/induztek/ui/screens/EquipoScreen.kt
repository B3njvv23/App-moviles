package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun EquipoScreen(ui: UiState, id: Long, ir: (String) -> Unit) {
    val d = ui.datos
    val e = d.equipos.firstOrNull { it.id == id } ?: return
    Contenido {
        Titulo(e.tag, "${e.tipo.texto} · ${e.tipo.clasificacion}")
        TarjetaSeccion("Ficha del equipo") {
            Text("${e.marca} ${e.modelo} · ${e.anio}")
            Text("Serie: ${e.serie}")
            Text(e.placa)
            if (e.maxContactos > 0) Text("Máximo contactos: ${e.maxContactos} µΩ")
            Text(e.observaciones)
        }
        Titulo("Historial", "Ordenado desde la prueba más reciente. Disponible sin señal.")
        val ps = d.protocolos.filter { it.equipoId == id }.sortedByDescending { it.fecha }
        ps.forEach { p ->
            TarjetaProtocolo(p, d, ui.usuario?.rol == Rol.CLIENTE) { ir("protocolo/${p.id}") }
        }
        if (ps.isEmpty()) Vacio("Aún no hay protocolos de este equipo.")
    }
}
