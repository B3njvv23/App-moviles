package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun ProtocolosScreen(ui: UiState, ir: (String) -> Unit) {
    var estado by rememberSaveable { mutableStateOf("") }
    var cliente by rememberSaveable { mutableStateOf("") }
    var tecnico by rememberSaveable { mutableStateOf("") }
    val d = ui.datos
    val esCliente = ui.usuario?.rol == Rol.CLIENTE
    Contenido {
        Titulo("Protocolos")
        if (!esCliente) {
            Selector(
                "Estado",
                estado,
                listOf("" to "Todos") + Estado.entries.map { it.name to it.texto },
                { estado = it },
            )
            Selector(
                "Cliente",
                cliente,
                listOf("" to "Todos") + d.clientes.map { it.id.toString() to it.nombre },
                { cliente = it },
            )
            Selector(
                "Autor",
                tecnico,
                listOf("" to "Todos") +
                    d.usuarios
                        .filter { it.rol != Rol.CLIENTE }
                        .map { it.id.toString() to it.nombre },
                { tecnico = it },
            )
        }
        val ps =
            d.protocolos
                .filter { p ->
                    val e = d.equipos.first { it.id == p.equipoId }
                    val i = d.instalaciones.first { it.id == e.instalacionId }
                    (estado.isBlank() || p.estado.name == estado) &&
                        (cliente.isBlank() || i.clienteId.toString() == cliente) &&
                        (tecnico.isBlank() || p.creadorId.toString() == tecnico)
                }
                .sortedByDescending { it.fecha }
        ps.forEach { p -> TarjetaProtocolo(p, d, esCliente) { ir("protocolo/${p.id}") } }
        if (ps.isEmpty()) Vacio("No hay protocolos para estos filtros.")
    }
}
