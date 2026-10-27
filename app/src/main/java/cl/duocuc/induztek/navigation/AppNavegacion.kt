package cl.duocuc.induztek.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.*
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.Rol
import cl.duocuc.induztek.ui.screens.*
import cl.duocuc.induztek.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavegacion(vm: SesionViewModel) {
    val acceso: AccesoViewModel =
        viewModel(factory = viewModelFactory { initializer { AccesoViewModel(vm) } })
    val protocolos: ProtocolosViewModel =
        viewModel(factory = viewModelFactory { initializer { ProtocolosViewModel(vm) } })
    val formulario: FormularioViewModel =
        viewModel(
            factory = viewModelFactory { initializer { FormularioViewModel(vm, protocolos) } }
        )
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(ui.mensaje) {
        if (ui.mensaje.isNotBlank()) {
            snack.showSnackbar(ui.mensaje)
            vm.consumir()
        }
    }
    if (!ui.listo) {
        Scaffold(snackbarHost = { SnackbarHost(snack) }) { p ->
            Box(
                Modifier.padding(p).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }
    key(ui.usuario?.id) {
        val nav = rememberNavController()
        val entrada by nav.currentBackStackEntryAsState()
        val ruta = entrada?.destination?.route.orEmpty()
        fun ir(destino: String) {
            nav.navigate(destino) { launchSingleTop = true }
        }
        fun form(tipo: String, ref: String = "", extra: String = "") {
            formulario.abrir(tipo, ref, extra)
            ir("formulario")
        }
        Scaffold(
            topBar = {
                if (ui.usuario != null)
                    TopAppBar(
                        title = {
                            Column {
                                Text("INDUZTEK", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "${ui.usuario!!.nombre.substringBefore(' ')} · ${ui.usuario!!.rol.texto}",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        },
                        navigationIcon = {
                            if (
                                ruta !in
                                    listOf("inicio", "equipos", "visitas", "protocolos", "gestion")
                            )
                                IconButton(onClick = { nav.popBackStack() }) {
                                    Icon(painterResource(R.drawable.ic_volver), "Volver")
                                }
                        },
                        actions = { TextButton(onClick = { vm.salir() }) { Text("Salir") } },
                    )
            },
            bottomBar = {
                if (
                    ui.usuario != null &&
                        ruta in listOf("inicio", "equipos", "visitas", "protocolos", "gestion")
                )
                    NavigationBar {
                        val destinos =
                            listOf(
                                "inicio" to "Inicio",
                                "equipos" to "Equipos",
                                "visitas" to "Visitas",
                                "protocolos" to "Protocolos",
                            ) +
                                if (ui.usuario!!.rol != Rol.CLIENTE) listOf("gestion" to "Gestión")
                                else emptyList()
                        destinos.forEach { (r, texto) ->
                            NavigationBarItem(
                                selected = ruta == r,
                                onClick = {
                                    nav.navigate(r) {
                                        popUpTo("inicio") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        painterResource(
                                            when (r) {
                                                "equipos" -> R.drawable.ic_buscar
                                                "visitas" -> R.drawable.ic_ubicacion
                                                "protocolos" -> R.drawable.ic_check
                                                "gestion" -> R.drawable.ic_persona
                                                else -> R.drawable.ic_menu
                                            }
                                        ),
                                        texto,
                                    )
                                },
                                label = { Text(texto, maxLines = 1) },
                            )
                        }
                    }
            },
            snackbarHost = { SnackbarHost(snack) },
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                AnimatedVisibility(ui.ocupado, enter = fadeIn(), exit = fadeOut()) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                NavHost(
                    nav,
                    startDestination = if (ui.usuario == null) "login" else "inicio",
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                ) {
                    composable("login") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        LoginScreen(
                            actual.ocupado,
                            acceso::login,
                            actual.erroresLogin,
                            acceso::limpiarLogin,
                            {
                                acceso.limpiarAcceso()
                                ir("registro")
                            },
                            {
                                acceso.limpiarAcceso()
                                ir("recuperar")
                            },
                        )
                    }
                    composable("registro") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        AccesoScreen(
                            true,
                            actual,
                            acceso::acceso,
                            {
                                acceso.limpiarAcceso()
                                nav.popBackStack()
                            },
                        )
                    }
                    composable("recuperar") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        AccesoScreen(
                            false,
                            actual,
                            acceso::acceso,
                            {
                                acceso.limpiarAcceso()
                                nav.popBackStack()
                            },
                        )
                    }
                    composable("inicio") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        InicioScreen(actual, ::ir, ::form)
                    }
                    composable("equipos") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        EquiposScreen(actual, ::ir, ::form)
                    }
                    composable("equipo/{id}") { e ->
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        EquipoScreen(
                            actual,
                            e.arguments?.getString("id")?.toLongOrNull() ?: 0L,
                            ::ir,
                        )
                    }
                    composable("visitas") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        VisitasScreen(actual, ::ir, ::form)
                    }
                    composable("visita/{id}") { e ->
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        VisitaScreen(
                            actual,
                            e.arguments?.getString("id")?.toLongOrNull() ?: 0L,
                            ::ir,
                            ::form,
                            vm::mensaje,
                        )
                    }
                    composable("protocolos") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        ProtocolosScreen(actual, ::ir)
                    }
                    composable("protocolo/{id}") { e ->
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        ProtocoloScreen(
                            actual,
                            e.arguments?.getString("id")?.toLongOrNull() ?: 0L,
                            ::form,
                            protocolos,
                        )
                    }
                    composable("gestion") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        GestionScreen(actual, ::form, protocolos)
                    }
                    composable("formulario") {
                        val actual by vm.ui.collectAsStateWithLifecycle()
                        val contador = remember { actual.guardado }
                        LaunchedEffect(actual.guardado) {
                            if (actual.guardado > contador) nav.popBackStack()
                        }
                        FormularioScreen(
                            actual.formulario,
                            actual.ocupado,
                            formulario::valor,
                            formulario::guardar,
                        )
                    }
                }
            }
        }
    }
}
