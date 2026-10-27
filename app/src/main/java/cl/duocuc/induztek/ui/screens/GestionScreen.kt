package cl.duocuc.induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.navigation.AbrirFormulario
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun GestionScreen(ui: UiState, form: AbrirFormulario, vm: ProtocolosViewModel) {
    val d = ui.datos
    var seccion by rememberSaveable { mutableStateOf("clientes") }
    Contenido {
        Titulo("Gestión", "Clientes, instalaciones, instrumentos y cuentas.")
        Selector(
            "Sección",
            seccion,
            listOf(
                "clientes" to "Clientes",
                "instalaciones" to "Instalaciones",
                "instrumentos" to "Instrumentos",
                "usuarios" to "Usuarios",
            ),
            { seccion = it },
        )
        when (seccion) {
            "clientes" -> {
                BotonPrincipal(
                    "Registrar cliente",
                    { form("cliente", "", "") },
                    Modifier.fillMaxWidth(),
                )
                d.clientes.forEach { c ->
                    TarjetaSeccion(c.nombre) {
                        Text("${c.rut} · ${c.giro}")
                        Text(c.contacto)
                        Text("${c.correo}\n${c.telefono}")
                        TextButton(onClick = { form("instalacion", c.id.toString(), "") }) {
                            Text("Agregar instalación")
                        }
                    }
                }
            }
            "instalaciones" -> {
                BotonPrincipal(
                    "Registrar instalación",
                    { form("instalacion", "", "") },
                    Modifier.fillMaxWidth(),
                )
                d.instalaciones.forEach { i ->
                    TarjetaSeccion(i.nombre) {
                        Text(d.clientes.firstOrNull { it.id == i.clienteId }?.nombre.orEmpty())
                        Text("${i.ubicacion} · ${i.comuna}, ${i.region}")
                        Text(i.acceso)
                        TextButton(onClick = { form("equipo", i.id.toString(), "") }) {
                            Text("Agregar equipo")
                        }
                    }
                }
            }
            "instrumentos" -> {
                if (ui.usuario?.rol == Rol.JEFE)
                    BotonPrincipal(
                        "Registrar instrumento",
                        { form("instrumento", "", "") },
                        Modifier.fillMaxWidth(),
                    )
                d.instrumentos.forEach { i ->
                    val vigente = i.vigente(LocalDate.now().toString())
                    val vence = i.calibraciones.maxOfOrNull { it.hasta }.orEmpty()
                    TarjetaSeccion(i.nombre) {
                        Text("${i.marca} ${i.modelo} · ${i.serie}")
                        Etiqueta(
                            if (vigente) "Calibración vigente" else "Calibración vencida",
                            !vigente,
                        )
                        Text("Vence: $vence")
                        if (
                            vigente &&
                                ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(vence)) <=
                                    30
                        )
                            Text("Vence dentro de 30 días", color = MaterialTheme.colorScheme.error)
                        i.calibraciones.forEach {
                            Text(
                                "${it.desde} → ${it.hasta}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (ui.usuario?.rol == Rol.JEFE)
                            TextButton(onClick = { form("instrumento", i.id.toString(), "") }) {
                                Text("Actualizar calibración")
                            }
                    }
                }
            }
            "usuarios" -> {
                if (ui.usuario?.rol == Rol.JEFE)
                    BotonPrincipal(
                        "Crear cuenta",
                        { form("usuario", "", "") },
                        Modifier.fillMaxWidth(),
                    )
                d.usuarios.forEach { u ->
                    TarjetaSeccion(u.nombre) {
                        Text(u.rol.texto)
                        Text(u.correo)
                        Etiqueta(if (u.activo) "Activo" else "Desactivado")
                        if (ui.usuario?.rol == Rol.JEFE && ui.usuario.id != u.id)
                            TextButton(onClick = { vm.activar(u) }) {
                                Text(if (u.activo) "Desactivar cuenta" else "Activar cuenta")
                            }
                    }
                }
            }
        }
    }
}
