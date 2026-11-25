package cl.duocuc.induztek.viewmodel
// Aquí manejo la sesión, SQLite y los cambios de la app.

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SesionViewModel internal constructor(
    app: Application,
    nombreDb: String,
    private val sincronizar: suspend (Datos) -> Unit,
) : AndroidViewModel(app) {
    @JvmOverloads
    constructor(app: Application, nombreDb: String = "induztek.db") :
        this(app, nombreDb, ApiInduztek::sincronizar)

    private class CampoPerfilInvalido(val campo: String, mensaje: String) : IllegalArgumentException(mensaje)

    internal lateinit var db: InduztekDbHelper
    internal lateinit var preferencias: SesionPreferencias
    private val _datos = MutableStateFlow(Datos())
    internal val datos = _datos.asStateFlow()
    internal var usuario: Usuario? = null
    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db = InduztekDbHelper(app, nombreDb)
                    preferencias =
                        SesionPreferencias(
                            app,
                            if (nombreDb == "induztek.db") "sesion" else "sesion_$nombreDb",
                        )
                    _datos.value =
                        db.cargar()
                            ?: run {
                                db.precargar(Semilla.crear())
                                requireNotNull(db.cargar())
                            }
                    usuario =
                        datos.value.usuarios.firstOrNull {
                            it.id == preferencias.leer() && it.activo
                        }
                }
                refrescar()
                sincronizarConServicios()
            } catch (e: Exception) {
                mensaje("No se pudo abrir la base local: ${e.message}")
            }
        }
    }

    internal fun refrescar() {
        _ui.update { it.copy(datos = visible(), usuario = usuario, listo = true) }
    }

    fun mensaje(s: String) {
        _ui.update { it.copy(mensaje = s) }
    }

    fun consumir() = mensaje("")

    fun accion(
        exito: String = "Cambios guardados en el dispositivo",
        errorEnFormulario: Boolean = false,
        alGuardar: () -> Unit = {},
        bloque: () -> Unit,
    ) {
        if (_ui.value.ocupado) return
        viewModelScope.launch {
            _ui.update { it.copy(ocupado = true) }
            try {
                withContext(Dispatchers.IO) { bloque() }
                refrescar()
                sincronizarConServicios()
                if (errorEnFormulario) _ui.update { it.copy(guardado = it.guardado + 1) }
                alGuardar()
                mensaje(exito)
            } catch (e: CampoPerfilInvalido) {
                marcarErroresPerfil(mapOf(e.campo to requireNotNull(e.message)))
            } catch (e: Exception) {
                if (errorEnFormulario)
                    _ui.update {
                        it.copy(
                            formulario =
                                it.formulario?.let { f ->
                                    f.copy(
                                        errores =
                                            f.errores +
                                                ("_general" to
                                                    (e.message
                                                        ?: "No se pudo guardar; revisa los datos"))
                                    )
                                }
                        )
                    }
                else mensaje(e.message ?: "No se pudo completar la acción")
            } finally {
                _ui.update { it.copy(ocupado = false) }
            }
        }
    }

    internal fun actualizar(cambio: (UiState) -> UiState) {
        _ui.update(cambio)
    }

    private fun sincronizarConServicios() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { sincronizar(datos.value) }
        }
    }

    fun salir() {
        usuario = null
        preferencias.borrar()
        refrescar()
    }

    internal fun recargar() {
        val d = requireNotNull(db.cargar()) { "No se pudieron leer los datos guardados" }
        _datos.value = d
        usuario = d.usuarios.firstOrNull { it.id == usuario?.id && it.activo }
        refrescar()
    }

    internal fun actual() = requireNotNull(usuario) { "Inicia sesión" }

    private fun marcarErroresPerfil(errores: Map<String, String>) {
        _ui.update { it.copy(erroresPerfil = errores) }
    }

    fun limpiarErrorPerfil(campo: String) {
        _ui.update { it.copy(erroresPerfil = it.erroresPerfil - campo) }
    }

    fun limpiarPerfil() = marcarErroresPerfil(emptyMap())

    fun actualizarPerfil(nombre: String, correo: String, claveActual: String, claveNueva: String) {
        if (_ui.value.ocupado) return
        val u = usuario ?: return mensaje("Inicia sesión para editar tu perfil")
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()
        val cambiarClave = claveActual.isNotEmpty() || claveNueva.isNotEmpty()
        val errores = buildMap {
            if (nombreLimpio.length < 3) put("nombre", "Escribe al menos 3 caracteres")
            if (!Reglas.correo(correoLimpio)) put("correo", "Ingresa un correo válido")
            else if (datos.value.usuarios.any { it.id != u.id && it.correo.equals(correoLimpio, true) })
                put("correo", "Este correo ya está registrado")
            if (cambiarClave) {
                if (claveActual.isBlank()) put("claveActual", "Ingresa tu contraseña actual")
                if (!Reglas.claveSegura(claveNueva))
                    put("claveNueva", "Usa 10 caracteres, mayúscula, número y símbolo")
            }
        }
        marcarErroresPerfil(errores)
        if (errores.isNotEmpty()) return

        accion(
            "Perfil actualizado",
            alGuardar = { _ui.update { it.copy(perfilGuardado = it.perfilGuardado + 1) } },
        ) {
            if (cambiarClave && !java.security.MessageDigest.isEqual(
                    Seguridad.hash(claveActual, u.sal).toByteArray(), u.hash.toByteArray(),
                ))
                throw CampoPerfilInvalido("claveActual", "La contraseña actual no coincide")
            val actualizado = if (!cambiarClave) u.copy(nombre = nombreLimpio, correo = correoLimpio)
            else {
                val sal = Seguridad.sal()
                u.copy(nombre = nombreLimpio, correo = correoLimpio, sal = sal, hash = Seguridad.hash(claveNueva, sal))
            }
            db.actualizarUsuario(actualizado)
            usuario = actualizado
            recargar()
        }
    }

    override fun onCleared() {
        if (::db.isInitialized) db.close()
        super.onCleared()
    }

    internal fun visible(): Datos {
        val d = _datos.value
        val u = usuario ?: return Datos()
        if (u.rol != Rol.CLIENTE) return d
        val instalaciones = d.instalaciones.filter { it.clienteId == u.clienteId }
        val equipos = d.equipos.filter { e -> instalaciones.any { it.id == e.instalacionId } }
        return d.copy(
            usuarios =
                d.usuarios
                    .filter { it.rol != Rol.CLIENTE || it.id == u.id }
                    .map { it.copy(hash = "", sal = "") },
            clientes = d.clientes.filter { it.id == u.clienteId },
            instalaciones = instalaciones,
            equipos = equipos,
            visitas =
                d.visitas.filter { v ->
                    v.cierre.isNotBlank() && instalaciones.any { it.id == v.instalacionId }
                },
            protocolos =
                d.protocolos.filter { p ->
                    equipos.any { it.id == p.equipoId } &&
                        p.estado !in listOf(Estado.BORRADOR, Estado.ANULADO)
                },
        )
    }
}
