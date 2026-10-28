package cl.duocuc.induztek

import android.app.Application
import android.content.Context
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.viewmodel.*

/** Prepara los ViewModels para comprobar el flujo desde las pruebas instrumentadas. */
internal class EscenarioLocal(context: Context, nombreDb: String = "induztek.db") {
    private val sesion = SesionViewModel(context.applicationContext as Application, nombreDb)
    private val acceso = AccesoViewModel(sesion)
    private val protocolos = ProtocolosViewModel(sesion)
    private val formulario = FormularioViewModel(sesion, protocolos)

    init {
        val limite = System.currentTimeMillis() + 15000
        while (!sesion.ui.value.listo && System.currentTimeMillis() < limite) Thread.sleep(20)
        check(sesion.ui.value.listo) { "La sesión local no se pudo inicializar" }
    }

    val db
        get() = sesion.db

    val datos
        get() = sesion.datos

    val usuario
        get() = sesion.usuario

    fun actual() = sesion.actual()

    fun visible() = sesion.visible()

    fun salir() = sesion.salir()

    fun login(correo: String, clave: String) = acceso.comprobarIngreso(correo, clave)

    fun solicitarCuenta(v: Map<String, String>) = acceso.solicitarCuenta(v)

    fun recuperarCuenta(correo: String, codigo: String, clave: String) =
        acceso.recuperarCuenta(correo, codigo, clave)

    fun cliente(c: Cliente) = formulario.cliente(c)

    fun instalacion(i: Instalacion) = formulario.instalacion(i)

    fun equipo(e: Equipo) = formulario.equipo(e)

    fun instrumento(i: Instrumento) = formulario.instrumento(i)

    fun visita(v: Visita) = formulario.visita(v)

    fun protocolo(p: Protocolo) = formulario.protocolo(p)

    fun editar(
        id: Long,
        temperatura: Double,
        humedad: Double,
        mantenimiento: String,
        observaciones: String,
    ) = formulario.editar(id, temperatura, humedad, mantenimiento, observaciones)

    fun medicion(id: Long, m: Medicion) = formulario.medicion(id, m)

    fun cerrar(id: Long, r: Recepcion) = formulario.cerrar(id, r)

    fun transicion(id: Long, nuevo: Estado, motivo: String) =
        protocolos.cambiarEstado(id, nuevo, motivo)

    fun evidencia(id: Long, foto: ByteArray) = protocolos.evidencia(id, foto)

    fun resultadoManual(id: Long, mid: Long, resultado: Resultado) =
        protocolos.resultadoManual(id, mid, resultado)

    fun usuario(u: Usuario) = protocolos.actualizarUsuario(u)
}
