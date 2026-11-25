package cl.duocuc.induztek.ui.screens
// Aquí muestro el resumen y edito el perfil.

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import cl.duocuc.induztek.viewmodel.Reglas

@Composable
fun InicioScreen(
    ui: UiState,
    ir: (String) -> Unit,
    form: AbrirFormulario,
    guardarPerfil: (String, String, String, String) -> Unit,
    limpiarPerfil: () -> Unit,
    limpiarError: (String) -> Unit,
) {
    var editar by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable(ui.usuario?.id) { mutableStateOf(ui.usuario?.nombre.orEmpty()) }
    var correo by rememberSaveable(ui.usuario?.id) { mutableStateOf(ui.usuario?.correo.orEmpty()) }
    var claveActual by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var actualVisible by remember { mutableStateOf(false) }
    var nuevaVisible by remember { mutableStateOf(false) }
    var guardadoAlAbrir by rememberSaveable { mutableIntStateOf(ui.perfilGuardado) }
    val errores = ui.erroresPerfil
    LaunchedEffect(ui.perfilGuardado) {
        if (editar && ui.perfilGuardado != guardadoAlAbrir) {
            editar = false
            claveActual = ""
            claveNueva = ""
        }
    }
    val d = ui.datos
    val cliente = ui.usuario?.rol == Rol.CLIENTE
    Contenido {
        Titulo(
            if (cliente) "Mis instalaciones" else "Tu jornada en terreno",
            if (cliente) "Consulta el respaldo y la evolución de tus equipos."
            else "Todo lo registrado se conserva en el dispositivo.",
        )
        BotonSecundario(
            "Editar perfil y cambiar contraseña",
            {
                limpiarPerfil()
                nombre = ui.usuario?.nombre.orEmpty()
                correo = ui.usuario?.correo.orEmpty()
                claveActual = ""
                claveNueva = ""
                actualVisible = false
                nuevaVisible = false
                guardadoAlAbrir = ui.perfilGuardado
                editar = true
            },
            Modifier.fillMaxWidth(),
        )
        if (editar) AlertDialog(
            onDismissRequest = { if (!ui.ocupado) editar = false },
            title = { Text("Mi perfil") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTexto(nombre, { nombre = it; limpiarError("nombre") }, "Nombre",
                        R.drawable.ic_persona, error = errores["nombre"])
                    CampoTexto(correo, { correo = it; limpiarError("correo") }, "Correo",
                        R.drawable.ic_correo, error = errores["correo"])
                    CampoContrasena(claveActual, { claveActual = it; limpiarError("claveActual") },
                        "Contraseña actual (para cambiarla)", actualVisible,
                        { actualVisible = !actualVisible }, error = errores["claveActual"], mostrarSeguridad = false)
                    CampoContrasena(claveNueva, { claveNueva = it; limpiarError("claveNueva") },
                        "Nueva contraseña", nuevaVisible, { nuevaVisible = !nuevaVisible }, error = errores["claveNueva"])
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { guardarPerfil(nombre, correo, claveActual, claveNueva) },
                    enabled = !ui.ocupado,
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editar = false }, enabled = !ui.ocupado) { Text("Cancelar") } },
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
