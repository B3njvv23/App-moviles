package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import cl.duocuc.induztek.viewmodel.Reglas

@Composable
fun InicioScreen(ui: UiState, ir: (String) -> Unit, form: AbrirFormulario) {
    val d = ui.datos
    val cliente = ui.usuario?.rol == Rol.CLIENTE
    Contenido {
        Titulo(
            if (cliente) "Mis instalaciones" else "Tu jornada en terreno",
            if (cliente) "Consulta el respaldo y la evolución de tus equipos."
            else "Todo lo registrado se conserva en el dispositivo.",
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TarjetaSeccion("Equipos", Modifier.weight(1f)) {
                Text("${d.equipos.size}", style = MaterialTheme.typography.headlineLarge)
            }
            TarjetaSeccion("Pendientes", Modifier.weight(1f)) {
                Text(
                    "${d.protocolos.count { it.estado in listOf(Estado.REGISTRADO, Estado.OBSERVADO) }}",
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
        if (!cliente) {
            BotonPrincipal(
                "Iniciar visita",
                { form("visita", "", "") },
                Modifier.fillMaxWidth(),
                icono = R.drawable.ic_ubicacion,
            )
            val atrasados = d.protocolos.count { Reglas.atrasado(it) }
            if (atrasados > 0) Etiqueta("$atrasados protocolos superan 2 días hábiles", true)
        }
        TarjetaSeccion("Accesos rápidos") {
            BotonSecundario(
                "Consultar equipos e historial",
                { ir("equipos") },
                Modifier.fillMaxWidth(),
            )
            BotonSecundario("Ver visitas y actas", { ir("visitas") }, Modifier.fillMaxWidth())
            BotonSecundario("Revisar protocolos", { ir("protocolos") }, Modifier.fillMaxWidth())
        }
        Titulo("Actividad reciente")
        d.protocolos
            .sortedByDescending { it.fecha }
            .take(3)
            .forEach { p -> TarjetaProtocolo(p, d, cliente) { ir("protocolo/${p.id}") } }
        if (d.protocolos.isEmpty()) Vacio("Crea una visita para comenzar el registro.")
    }
}
