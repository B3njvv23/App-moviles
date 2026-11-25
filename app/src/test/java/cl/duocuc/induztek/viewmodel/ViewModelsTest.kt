package cl.duocuc.induztek.viewmodel
// Aquí pruebo el acceso, la sesión y los formularios con una base SQLite temporal.

import android.graphics.Bitmap
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import cl.duocuc.induztek.data.ClimaActual
import cl.duocuc.induztek.model.*
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class ViewModelsTest {
    private val almacenes = mutableListOf<ViewModelStore>()
    private val nombreDb = "viewmodels-prueba.db"
    private val claveInicial = "Induztek2026!"
    private val claveNueva = "NuevaClave2026!"
    private val tecnico = "mgodoy@induztek.ejemplo"
    private val jefe = "respinoza@induztek.ejemplo"
    private val ingeniero = "acontreras@induztek.ejemplo"
    private val cliente = "mfuentes@envpacifico.ejemplo"
    private lateinit var sesion: SesionViewModel
    private lateinit var acceso: AccesoViewModel
    private lateinit var protocolos: ProtocolosViewModel
    private lateinit var formulario: FormularioViewModel

    @Before
    fun preparar() {
        RuntimeEnvironment.getApplication().deleteDatabase(nombreDb)
        sesion = abrirSesion()
        acceso = AccesoViewModel(sesion)
        protocolos = ProtocolosViewModel(sesion) { ClimaActual(23.0, 0, 10.0) }
        formulario = FormularioViewModel(sesion, protocolos)
        almacenes.last().put("acceso", acceso)
        almacenes.last().put("protocolos", protocolos)
        almacenes.last().put("formulario", formulario)
    }

    @After
    fun cerrar() {
        almacenes.asReversed().forEach { it.clear() }
        RuntimeEnvironment.getApplication().deleteDatabase(nombreDb)
    }

    private fun abrirSesion(): SesionViewModel {
        val nueva = SesionViewModel(RuntimeEnvironment.getApplication(), nombreDb) {}
        almacenes.add(ViewModelStore().apply { put("sesion", nueva) })
        esperar({ nueva.ui.value.mensaje }) { nueva.ui.value.listo }
        return nueva
    }

    private fun esperar(mensaje: () -> String = { sesion.ui.value.mensaje }, condicion: () -> Boolean) {
        val limite = System.nanoTime() + 30_000_000_000L
        while (System.nanoTime() < limite) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condicion()) return
            Thread.sleep(5)
        }
        fail("La operación no terminó. Estado: " + mensaje())
    }

    private fun entrar(correo: String, clave: String = claveInicial) {
        acceso.login(correo, clave)
        esperar { !sesion.ui.value.ocupado && sesion.ui.value.usuario?.correo == correo }
    }

    private fun guardar(tipo: String, ref: String = "", extra: String = "", campos: Map<String, String>) {
        formulario.abrir(tipo, ref, extra)
        campos.forEach { (campo, valor) -> formulario.valor(campo, valor) }
        val anterior = sesion.ui.value.guardado
        formulario.guardar()
        esperar { !sesion.ui.value.ocupado }
        assertEquals(sesion.ui.value.formulario?.errores.toString(), anterior + 1, sesion.ui.value.guardado)
    }

    @Test
    fun loginValidaCamposRestauraLaSesionYRestringeLaVistaDelCliente() {
        acceso.login("inválido", "")
        assertEquals(setOf("correo", "clave"), acceso.ui.value.erroresLogin.keys)
        acceso.limpiarLogin()
        assertTrue(acceso.ui.value.erroresLogin.isEmpty())
        acceso.login(cliente, "Incorrecta123!")
        esperar { acceso.ui.value.erroresLogin.containsKey("clave") }
        assertNull(sesion.ui.value.usuario)

        entrar(cliente)
        val visible = sesion.visible()
        assertEquals(listOf(1L), visible.clientes.map { it.id })
        assertTrue(visible.equipos.all { it.instalacionId == 1L })
        assertTrue(visible.usuarios.all { it.hash.isEmpty() && it.sal.isEmpty() })
        assertEquals(cliente, abrirSesion().ui.value.usuario?.correo)
        sesion.consumir()
        assertEquals("", sesion.ui.value.mensaje)
        sesion.salir()
        assertEquals(Datos(), sesion.ui.value.datos)
        assertNull(abrirSesion().ui.value.usuario)
    }

    @Test
    fun registroYRecuperacionDevuelvenErroresYElCodigoSoloSirveUnaVez() {
        entrar(jefe)
        guardar("cliente", campos = mapOf(
            "nombre" to "Empresa de prueba", "rut" to "22.222.222-2", "giro" to "Industria",
            "contacto" to "Contacto QA", "telefono" to "912345678", "correo" to "empresa@prueba.cl",
        ))
        sesion.salir()
        acceso.acceso(true, emptyMap())
        assertTrue(acceso.ui.value.erroresAcceso.isNotEmpty())
        acceso.limpiarAcceso()
        val correo = "nuevo@prueba.cl"
        val solicitud = mapOf(
            "nombre" to "Cuenta de prueba", "rut" to "11.111.111-1", "empresa" to "22.222.222-2",
            "correo" to correo, "clave" to claveInicial, "confirmar" to claveInicial,
        )
        acceso.acceso(true, solicitud)
        esperar { !acceso.ui.value.ocupado }
        val codigo = acceso.ui.value.resultadoAcceso
        assertTrue(acceso.ui.value.erroresAcceso.toString(), codigo.isNotBlank())
        assertFalse(sesion.datos.value.usuarios.first { it.correo == correo }.activo)
        acceso.acceso(true, solicitud)
        esperar { !acceso.ui.value.ocupado }
        assertTrue(acceso.ui.value.erroresAcceso.getValue("_general").contains("ya está registrado"))

        val recuperacion = mapOf("correo" to correo, "clave" to claveNueva, "confirmar" to claveNueva)
        acceso.acceso(false, recuperacion + ("codigo" to "código equivocado"))
        esperar { !acceso.ui.value.ocupado }
        assertTrue(acceso.ui.value.erroresAcceso.containsKey("_general"))
        acceso.acceso(false, recuperacion + ("codigo" to codigo))
        esperar { !acceso.ui.value.ocupado }
        assertEquals("OK", acceso.ui.value.resultadoAcceso)
        acceso.acceso(false, recuperacion + ("codigo" to codigo))
        esperar { !acceso.ui.value.ocupado }
        assertTrue(acceso.ui.value.erroresAcceso.containsKey("_general"))

        entrar(jefe)
        protocolos.activar(sesion.datos.value.usuarios.first { it.correo == correo })
        esperar { !sesion.ui.value.ocupado }
        sesion.salir()
        entrar(correo, claveNueva)
        assertEquals(Rol.CLIENTE, sesion.actual().rol)
    }

    @Test
    fun perfilMuestraErroresPorCampoYConservaLosCambiosValidos() {
        entrar(tecnico)
        sesion.actualizarPerfil("A", "mal", "", "corta")
        assertEquals(setOf("nombre", "correo", "claveActual", "claveNueva"), sesion.ui.value.erroresPerfil.keys)
        sesion.limpiarErrorPerfil("nombre")
        assertFalse(sesion.ui.value.erroresPerfil.containsKey("nombre"))
        sesion.limpiarPerfil()
        assertTrue(sesion.ui.value.erroresPerfil.isEmpty())
        sesion.actualizarPerfil("Nombre Válido", jefe, "", "")
        assertTrue(sesion.ui.value.erroresPerfil.containsKey("correo"))

        sesion.actualizarPerfil("Nombre Válido", tecnico, "Incorrecta", claveNueva)
        esperar { !sesion.ui.value.ocupado }
        assertEquals("La contraseña actual no coincide", sesion.ui.value.erroresPerfil["claveActual"])
        assertEquals(0, sesion.ui.value.perfilGuardado)
        sesion.actualizarPerfil(" Nombre Válido ", tecnico, "", "")
        esperar { !sesion.ui.value.ocupado }
        assertEquals(1, sesion.ui.value.perfilGuardado)
        assertEquals("Nombre Válido", sesion.actual().nombre)
        sesion.actualizarPerfil("Nombre Válido", "nuevo.tecnico@prueba.cl", claveInicial, claveNueva)
        esperar { !sesion.ui.value.ocupado }
        assertEquals(2, sesion.ui.value.perfilGuardado)
        sesion.salir()
        entrar("nuevo.tecnico@prueba.cl", claveNueva)
        assertEquals("Nombre Válido", sesion.actual().nombre)
    }

    @Test
    fun formulariosValidanYGuardanElCatalogoYLosUsuarios() {
        entrar(tecnico)
        formulario.abrir("instalacion")
        formulario.valor("cliente", "")
        formulario.guardar()
        assertTrue(formulario.ui.value.formulario!!.errores.containsKey("cliente"))
        formulario.valor("cliente", "1")
        assertFalse(formulario.ui.value.formulario!!.errores.containsKey("cliente"))
        guardar("instalacion", campos = mapOf("cliente" to "1", "nombre" to "Nueva planta",
            "ubicacion" to "Sector 1", "comuna" to "Lampa"))
        val instalacion = sesion.datos.value.instalaciones.first { it.nombre == "Nueva planta" }.id
        guardar("equipo", ref = instalacion.toString(), campos = mapOf(
            "tag" to "tr-qa", "tipo" to "TRANSFORMADOR", "marca" to "QA", "serie" to "TR-2026",
            "anio" to "2020", "placa" to "15 kV",
        ))
        assertTrue(sesion.datos.value.equipos.any { it.instalacionId == instalacion && it.tag == "TR-QA" })

        formulario.abrir("instrumento")
        val camposInstrumento = mapOf("nombre" to "Medidor QA", "marca" to "QA", "modelo" to "Uno", "serie" to "MED-QA")
        camposInstrumento.forEach { (campo, valor) -> formulario.valor(campo, valor) }
        formulario.guardar()
        esperar { !sesion.ui.value.ocupado }
        assertTrue(formulario.ui.value.formulario!!.errores.getValue("_general").contains("jefe técnico"))
        entrar(jefe)
        guardar("instrumento", campos = camposInstrumento)
        val instrumento = sesion.datos.value.instrumentos.first { it.serie == "MED-QA" }
        guardar("instrumento", ref = instrumento.id.toString(), campos = mapOf(
            "hasta" to LocalDate.now().plusYears(2).toString(),
        ))
        assertEquals(2, sesion.datos.value.instrumentos.first { it.id == instrumento.id }.calibraciones.size)
        guardar("usuario", campos = mapOf("nombre" to "Cuenta interna", "rut" to "11.111.111-1",
            "correo" to "interna@prueba.cl", "rol" to "TECNICO", "clave" to claveNueva))
        assertTrue(sesion.datos.value.usuarios.any { it.correo == "interna@prueba.cl" })
        protocolos.activar(sesion.actual())
        esperar { !sesion.ui.value.ocupado }
        assertTrue(protocolos.ui.value.mensaje.contains("No puedes desactivar"))
    }

    @Test
    fun recorridoDeVisitaGuardaMedicionesEvidenciaYFormalizacion() {
        entrar(tecnico)
        guardar("visita", campos = mapOf("instalacion" to "1", "ot" to "OT-PRUEBA",
            "ingeniero" to "5", "mantenimiento" to "si", "fecha" to "2026-09-26"))
        val visita = sesion.datos.value.visitas.first { it.ot == "OT-PRUEBA" }
        guardar("protocolo", ref = visita.id.toString(), campos = mapOf("equipo" to "1", "temperatura" to "20",
            "humedad" to "60", "mantenimiento" to "Limpieza", "reemplaza" to "PT-2024-0312"))
        val id = sesion.datos.value.protocolos.first { it.visitaId == visita.id }.id
        val ref = id.toString()
        fun protocolo() = sesion.datos.value.protocolos.first { it.id == id }
        guardar("editar", ref, campos = mapOf("temperatura" to "21", "obs" to "Equipo disponible"))
        assertEquals(21.0, protocolo().temperatura, 0.0)
        protocolos.registrar(id)
        esperar { !sesion.ui.value.ocupado }
        assertEquals(Estado.BORRADOR, protocolo().estado)
        assertTrue(sesion.ui.value.mensaje.contains("Completa cada prueba"))

        for (prueba in Reglas.pruebas(Tipo.TRANSFORMADOR)) {
            for (elemento in Reglas.elementos(Tipo.TRANSFORMADOR, prueba)) {
                val campos = when {
                    prueba == Prueba.AISLACION && elemento == "AT-T" -> mapOf(
                        "v" to "5", "r1" to "2", "r10" to "5", "unidad" to "G", "instrumento" to "1",
                    )
                    prueba == Prueba.DEVANADOS && elemento == "AT-U" -> mapOf("r" to "3", "instrumento" to "1")
                    else -> mapOf("realizada" to "no", "motivo" to "Sin ventana de trabajo")
                }
                guardar("medicion", ref, prueba.name + "|" + elemento, campos)
            }
        }
        formulario.abrir("medicion", ref, "AISLACION|AT-T")
        assertEquals("2000.0", formulario.ui.value.formulario!!.valores["r1"])
        val foto = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888)
        protocolos.foto(id, foto)
        esperar { !sesion.ui.value.ocupado }
        assertTrue(protocolo().evidencia!!.isNotEmpty())
        protocolos.registrar(id)
        esperar { !sesion.ui.value.ocupado }
        assertEquals(Estado.REGISTRADO, protocolo().estado)
        guardar("cerrar", visita.id.toString(), campos = mapOf(
            "nombre" to "Encargada de terreno", "rut" to "11.111.111-1", "cargo" to "Supervisora",
        ))
        assertTrue(sesion.datos.value.visitas.first { it.id == visita.id }.acta.isNotEmpty())

        entrar(ingeniero)
        val manual = protocolo().mediciones.first { it.prueba == Prueba.DEVANADOS && it.realizada }
        protocolos.manual(id, manual.id, Resultado.APROBADO)
        esperar { !sesion.ui.value.ocupado }
        assertEquals(Resultado.APROBADO, protocolo().mediciones.first { it.id == manual.id }.resultado)
        guardar("estado", ref, "FORMALIZADO", mapOf("motivo" to "Verificación de la visita completa"))
        assertEquals(Estado.FORMALIZADO, protocolo().estado)
        assertThrows(IllegalArgumentException::class.java) { protocolos.cambiarEstado(id, Estado.ANULADO, "Prueba") }
    }

    @Test
    fun protocolosPresentaElClimaYControlaLosEstados() {
        protocolos.cargarClima()
        esperar { protocolos.clima.isNotBlank() }
        assertTrue(protocolos.clima.contains("23.0 °C · despejado"))
        protocolos.cargarClima()
        protocolos.mensaje("Clima consultado")
        assertEquals("Clima consultado", sesion.ui.value.mensaje)
        val estados = mapOf(
            2 to "parcialmente nublado", 45 to "neblina", 55 to "lluvia ligera",
            75 to "nieve", 95 to "condiciones variables",
        )
        estados.forEach { (codigo, texto) ->
            val conClima = ProtocolosViewModel(sesion) { ClimaActual(23.0, codigo, 10.0) }
            almacenes.last().put("clima-$codigo", conClima)
            conClima.cargarClima()
            esperar { conClima.clima.isNotBlank() }
            assertTrue(conClima.clima.contains(texto))
        }
        val sinConexion = ProtocolosViewModel(sesion) { error("Sin conexión") }
        almacenes.last().put("sinConexion", sinConexion)
        sinConexion.cargarClima()
        esperar { sinConexion.clima.isNotBlank() }
        assertEquals("Clima no disponible sin conexión", sinConexion.clima)

        entrar(tecnico)
        assertThrows(IllegalArgumentException::class.java) {
            protocolos.cambiarEstado(3L, Estado.OBSERVADO, "Corrección")
        }
        entrar(ingeniero)
        guardar("estado", "3", "OBSERVADO", mapOf("motivo" to "Revisar valores"))
        assertEquals(Estado.OBSERVADO, sesion.datos.value.protocolos.first { it.id == 3L }.estado)
        guardar("estado", "3", "ANULADO", mapOf("motivo" to "Equipo retirado"))
        assertEquals(Estado.ANULADO, sesion.datos.value.protocolos.first { it.id == 3L }.estado)
    }
}
