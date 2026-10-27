package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun EquiposScreen(ui: UiState, ir: (String) -> Unit, form: AbrirFormulario) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var clase by rememberSaveable { mutableStateOf("") }
    val d = ui.datos
    val equipos =
        d.equipos.filter { e ->
            (clase.isEmpty() || e.tipo.clasificacion == clase) &&
                "$e ${d.instalaciones.firstOrNull { it.id == e.instalacionId }?.nombre}"
                    .contains(busqueda, true)
        }
    Contenido {
        Titulo("Equipos", "Encuentra un equipo y compara sus pruebas anteriores.")
        CampoTexto(
            busqueda,
            { busqueda = it },
            "Buscar TAG, serie o instalación",
            R.drawable.ic_buscar,
        )
        Selector(
            "Clasificación",
            clase,
            listOf("" to "Todos", "Primario" to "Primarios", "Secundario" to "Secundarios"),
            { clase = it },
        )
        if (ui.usuario?.rol != Rol.CLIENTE)
            BotonPrincipal("Registrar equipo", { form("equipo", "", "") }, Modifier.fillMaxWidth())
        equipos.forEach { e ->
            TarjetaSeccion(e.tag, Modifier.clickable { ir("equipo/${e.id}") }) {
                Text("${e.tipo.texto} · ${e.tipo.clasificacion}")
                Text(d.instalaciones.firstOrNull { it.id == e.instalacionId }?.nombre.orEmpty())
                Text("${e.marca} · Serie ${e.serie}", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { ir("equipo/${e.id}") }) { Text("Ver historial") }
            }
        }
        if (equipos.isEmpty()) Vacio("No hay equipos que coincidan con la búsqueda.")
    }
}
