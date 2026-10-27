package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun VisitasScreen(ui: UiState, ir: (String) -> Unit, form: AbrirFormulario) {
    val d = ui.datos
    Contenido {
        Titulo("Visitas y actas")
        if (ui.usuario?.rol != Rol.CLIENTE)
            BotonPrincipal("Iniciar visita", { form("visita", "", "") }, Modifier.fillMaxWidth())
        d.visitas
            .sortedByDescending { it.fecha }
            .forEach { v ->
                TarjetaSeccion(v.ot, Modifier.clickable { ir("visita/${v.id}") }) {
                    Text(d.instalaciones.firstOrNull { it.id == v.instalacionId }?.nombre.orEmpty())
                    Text(v.fecha)
                    Etiqueta(if (v.cierre.isBlank()) "Abierta" else "Cerrada · acta disponible")
                    TextButton(onClick = { ir("visita/${v.id}") }) { Text("Ver visita") }
                }
            }
        if (d.visitas.isEmpty()) Vacio("Las visitas cerradas y sus actas aparecerán aquí.")
    }
}
