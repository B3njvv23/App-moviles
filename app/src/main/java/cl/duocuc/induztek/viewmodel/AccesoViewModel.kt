package cl.duocuc.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.viewmodel.*
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccesoViewModel(private val sesion: SesionViewModel) : ViewModel() {
    val ui
        get() = sesion.ui

    private val db
        get() = sesion.db

    fun limpiarLogin() {
        sesion.actualizar { it.copy(erroresLogin = emptyMap()) }
    }

    fun limpiarAcceso() {
        sesion.actualizar { it.copy(erroresAcceso = emptyMap(), resultadoAcceso = "") }
    }

    fun acceso(registro: Boolean, valores: Map<String, String>) {
        if (ui.value.ocupado) return
        val errores = cl.duocuc.induztek.viewmodel.ValidacionAcceso.validar(registro, valores)
        sesion.actualizar { it.copy(erroresAcceso = errores, resultadoAcceso = "") }
        if (errores.isNotEmpty()) return
        viewModelScope.launch {
            sesion.actualizar { it.copy(ocupado = true) }
            try {
                val resultado =
                    withContext(Dispatchers.IO) {
                        if (registro) solicitarCuenta(valores)
                        else {
                            recuperarCuenta(
                                valores.getValue("correo"),
                                valores.getValue("codigo"),
                                valores.getValue("clave"),
                            )
                            "OK"
                        }
                    }
                sesion.actualizar { it.copy(resultadoAcceso = resultado) }
            } catch (e: Exception) {
                sesion.actualizar {
                    it.copy(
                        erroresAcceso =
                            mapOf("_general" to (e.message ?: "No se pudo completar la solicitud"))
                    )
                }
            } finally {
                sesion.actualizar { it.copy(ocupado = false) }
            }
        }
    }

    fun login(c: String, p: String) {
        val errores =
            Reglas.validarFormulario(
                "login",
                mapOf("correo" to c.trim(), "clave" to p),
                listOf("correo", "clave"),
            )
        if (errores.isNotEmpty()) {
            sesion.actualizar { it.copy(erroresLogin = errores) }
            return
        }
        if (ui.value.ocupado) return
        viewModelScope.launch {
            sesion.actualizar { it.copy(ocupado = true, erroresLogin = emptyMap()) }
            try {
                withContext(Dispatchers.IO) { comprobarIngreso(c, p) }
                sesion.refrescar()
                sesion.mensaje("Sesión iniciada")
            } catch (e: Exception) {
                sesion.actualizar {
                    it.copy(
                        erroresLogin =
                            mapOf("clave" to "Correo o contraseña incorrectos; revisa ambos datos")
                    )
                }
            } finally {
                sesion.actualizar { it.copy(ocupado = false) }
            }
        }
    }

    internal fun comprobarIngreso(correo: String, clave: String) {
        val u =
            sesion.datos.value.usuarios.firstOrNull {
                it.correo.equals(correo.trim(), true) && it.activo
            }
        require(
            u != null &&
                MessageDigest.isEqual(
                    Seguridad.hash(clave, u.sal).toByteArray(),
                    u.hash.toByteArray(),
                )
        ) {
            "Correo o contraseña incorrectos"
        }
        sesion.usuario = u
        sesion.preferencias.guardar(u.id)
    }

    @Synchronized
    internal fun solicitarCuenta(v: Map<String, String>): String {
        require(cl.duocuc.induztek.viewmodel.ValidacionAcceso.validar(true, v).isEmpty()) {
            "Revisa los campos del registro"
        }
        val d = sesion.datos.value
        require(
            d.usuarios.none {
                it.correo.equals(v.getValue("correo").trim(), true) ||
                    normalRut(it.rut) == normalRut(v.getValue("rut"))
            }
        ) {
            "El correo o RUT ya está registrado. Recupera tu contraseña o consulta al jefe técnico."
        }
        val empresa =
            d.clientes.firstOrNull { normalRut(it.rut) == normalRut(v.getValue("empresa")) }
        requireNotNull(empresa) {
            "La empresa debe estar registrada por el jefe técnico. Revisa su RUT."
        }
        val sal = Seguridad.sal()
        val u =
            Usuario(
                nombre = v.getValue("nombre").trim(),
                rut = v.getValue("rut").trim(),
                correo = v.getValue("correo").trim(),
                rol = Rol.CLIENTE,
                clienteId = empresa.id,
                activo = false,
                hash = Seguridad.hash(v.getValue("clave"), sal),
                sal = sal,
            )
        val codigo = java.util.UUID.randomUUID().toString()
        val usuarioId = db.insertarUsuario(u)
        db.meta("recuperacion_$usuarioId", Seguridad.hash(codigo, sal))
        sesion.recargar()
        return codigo
    }

    @Synchronized
    internal fun recuperarCuenta(correo: String, codigo: String, clave: String) {
        require(clave.length >= 10) { "Usa al menos 10 caracteres" }
        val u = sesion.datos.value.usuarios.firstOrNull { it.correo.equals(correo.trim(), true) }
        require(
            u != null &&
                codigo.isNotBlank() &&
                db.meta("recuperacion_${u.id}").orEmpty().let {
                    it.isNotEmpty() &&
                        MessageDigest.isEqual(
                            it.toByteArray(),
                            Seguridad.hash(codigo.trim(), u.sal).toByteArray(),
                        )
                }
        ) {
            "Correo o código de recuperación incorrectos"
        }
        db.actualizarUsuario(u.copy(hash = Seguridad.hash(clave, u.sal)))
        sesion.recargar()
        // El código no habilita una cuenta pendiente ni cambia su perfil.
    }

    private fun normalRut(s: String) = s.replace(".", "").replace("-", "").uppercase()
}
