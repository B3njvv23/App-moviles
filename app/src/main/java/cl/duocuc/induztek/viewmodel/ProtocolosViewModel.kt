package cl.duocuc.induztek.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.induztek.data.*
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.viewmodel.*

class ProtocolosViewModel(private val sesion: SesionViewModel) : ViewModel() {
    val ui
        get() = sesion.ui

    private val db
        get() = sesion.db

    fun mensaje(texto: String) = sesion.mensaje(texto)

    fun registrar(id: Long) = sesion.accion {
        cambiarEstado(id, Estado.REGISTRADO, "Registro completo en terreno")
    }

    fun manual(id: Long, mid: Long, r: Resultado) = sesion.accion {
        resultadoManual(id, mid, r)
    }

    fun foto(id: Long, foto: android.graphics.Bitmap) =
        sesion.accion("Evidencia guardada") { evidencia(id, ConvertidorImagen.aBytes(foto)) }

    fun activar(u: Usuario) = sesion.accion { actualizarUsuario(u.copy(activo = !u.activo)) }

    private fun normalRut(s: String) = s.replace(".", "").replace("-", "").uppercase()

    private fun reemplazar(p: Protocolo, motivo: String) {
        val cambiado =
            p.copy(historial = p.historial + Cambio(sesion.actual().id, p.estado, motivo = motivo))
        db.actualizarProtocolo(cambiado)
        sesion.recargar()
    }

    internal fun evidencia(id: Long, foto: ByteArray) {
        val p = sesion.datos.value.protocolos.first { it.id == id }
        require(Reglas.editable(sesion.actual(), p))
        reemplazar(p.copy(evidencia = foto), "Evidencia fotográfica agregada")
    }

    internal fun resultadoManual(id: Long, medicionId: Long, resultado: Resultado) {
        require(sesion.actual().rol in listOf(Rol.INGENIERO, Rol.JEFE)) {
            "Solo el ingeniero o jefe técnico"
        }
        val d = sesion.datos.value
        val p = d.protocolos.first { it.id == id }
        val e = d.equipos.first { it.id == p.equipoId }
        require(p.estado in listOf(Estado.BORRADOR, Estado.OBSERVADO, Estado.REGISTRADO))
        val m = p.mediciones.first { it.id == medicionId }
        require(m.realizada && Reglas.evaluar(e.tipo, m.prueba, m.valores, e.maxContactos) == null)
        reemplazar(
            p.copy(
                mediciones =
                    p.mediciones.map {
                        if (it.id == medicionId) it.copy(resultado = resultado) else it
                    }
            ),
            "Resultado manual ${m.elemento}: ${resultado.texto}",
        )
    }

    internal fun cambiarEstado(id: Long, nuevo: Estado, motivo: String) {
        val d = sesion.datos.value
        val p = d.protocolos.first { it.id == id }
        val u = sesion.actual()
        val e = d.equipos.first { it.id == p.equipoId }
        require(p.estado !in listOf(Estado.FORMALIZADO, Estado.ANULADO)) {
            "El protocolo es inmutable"
        }
        when (nuevo) {
            Estado.REGISTRADO -> {
                require(Reglas.editable(u, p)) { "Solo el autor puede registrar" }
                require(Reglas.pendiente(p, e.tipo).isEmpty()) {
                    "Completa cada prueba o marca no realizada con motivo"
                }
                require(p.mediciones.any { it.realizada }) {
                    "Al menos una prueba debe estar realizada"
                }
                if (d.visitas.first { it.id == p.visitaId }.mantenimiento)
                    require(p.mantenimiento.isNotBlank()) {
                        "Describe el mantenimiento realizado al equipo"
                    }
            }
            Estado.FORMALIZADO,
            Estado.OBSERVADO -> {
                require(u.rol in listOf(Rol.INGENIERO, Rol.JEFE)) {
                    "Solo ingeniero o jefe técnico"
                }
                require(p.estado == Estado.REGISTRADO) {
                    "Primero registra el protocolo en terreno"
                }
                if (nuevo == Estado.FORMALIZADO) {
                    require(d.visitas.first { it.id == p.visitaId }.cierre.isNotBlank()) {
                        "Cierra la visita y entrega el acta antes de formalizar"
                    }
                    require(
                        Reglas.vigentes(p).filter { it.realizada }.all { it.resultado != null }
                    ) {
                        "Completa los resultados manuales"
                    }
                    if (Reglas.significativas(p, d).isNotEmpty())
                        require(motivo.isNotBlank()) { "Comenta la variación significativa" }
                } else require(motivo.isNotBlank()) { "Motivo obligatorio" }
            }
            Estado.ANULADO -> {
                require(u.rol in listOf(Rol.INGENIERO, Rol.JEFE))
                require(motivo.isNotBlank()) { "Motivo obligatorio" }
            }
            else -> error("Transición inválida")
        }
        reemplazar(p.copy(estado = nuevo), motivo)
    }

    internal fun actualizarUsuario(u: Usuario) {
        require(sesion.actual().rol == Rol.JEFE) { "Solo jefe técnico" }
        require(Reglas.rut(u.rut) && Reglas.correo(u.correo))
        val d = sesion.datos.value
        require(
            d.usuarios.none {
                it.id != u.id &&
                    (it.correo.equals(u.correo, true) || normalRut(it.rut) == normalRut(u.rut))
            }
        ) {
            "RUT o correo ya registrado"
        }
        if (u.rol == Rol.CLIENTE) require(d.clientes.any { it.id == u.clienteId })
        require(u.id != sesion.actual().id || u.activo) { "No puedes desactivar tu propia sesión" }
        if (u.id == 0L) db.insertarUsuario(u) else db.actualizarUsuario(u)
        sesion.recargar()
    }
}
