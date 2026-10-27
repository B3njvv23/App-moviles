package cl.duocuc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SesionViewModel
@JvmOverloads
constructor(app: Application, nombreDb: String = "induztek.db") : AndroidViewModel(app) {
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
                    _datos.value = db.cargar() ?: Datos()
                    usuario =
                        datos.value.usuarios.firstOrNull {
                            it.id == preferencias.leer() && it.activo
                        }
                }
                refrescar()
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
        bloque: () -> Unit,
    ) {
        if (_ui.value.ocupado) return
        viewModelScope.launch {
            _ui.update { it.copy(ocupado = true) }
            try {
                withContext(Dispatchers.IO) { bloque() }
                refrescar()
                if (errorEnFormulario) _ui.update { it.copy(guardado = it.guardado + 1) }
                mensaje(exito)
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
