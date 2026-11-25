package cl.duocuc.induztek.data
// Aquí llamo a los servicios y consulto el clima.
import cl.duocuc.induztek.BuildConfig
import cl.duocuc.induztek.model.Datos
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface CatalogoApi {
    @PUT("api/catalogo/sync")
    suspend fun sincronizar(@Body datos: Map<String, List<Map<String, String>>>)
}

interface ProtocolosApi {
    @PUT("api/protocolos/sync")
    suspend fun sincronizar(@Body protocolos: List<Map<String, String>>)
}

interface ClimaApi {
    @GET("v1/forecast?latitude=-33.4489&longitude=-70.6693&current=temperature_2m,weather_code,wind_speed_10m&timezone=America%2FSantiago")
    suspend fun consultar(): ClimaRespuesta
}

data class ClimaRespuesta(val current: ClimaActual)
data class ClimaActual(val temperature_2m: Double, val weather_code: Int, val wind_speed_10m: Double)

object ApiInduztek {
    private val gson = Gson()
    private val catalogo = crear(BuildConfig.CATALOGO_URL).create(CatalogoApi::class.java)
    private val protocolos = crear(BuildConfig.PROTOCOLOS_URL).create(ProtocolosApi::class.java)
    private val clima = crear("https://api.open-meteo.com/").create(ClimaApi::class.java)

    private fun crear(url: String) = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    suspend fun sincronizar(datos: Datos) {
        val categorias = mapOf(
            "clientes" to datos.clientes,
            "instalaciones" to datos.instalaciones,
            "equipos" to datos.equipos,
            "visitas" to datos.visitas,
        ).mapValues { (_, filas) -> filas.map { mapOf("id" to id(it), "contenido" to gson.toJson(it)) } }
        catalogo.sincronizar(categorias)
        protocolos.sincronizar(datos.protocolos.map { protocolo ->
            val fila = mutableMapOf("id" to id(protocolo), "contenido" to gson.toJson(protocolo))
            protocolo.evidencia?.let { foto ->
                if (foto.isNotEmpty()) fila["fotografia"] = android.util.Base64.encodeToString(foto, android.util.Base64.NO_WRAP)
            }
            fila
        })
    }

    suspend fun consultarClima(): ClimaActual = clima.consultar().current

    private fun id(fila: Any): String = when (fila) {
        is cl.duocuc.induztek.model.Cliente -> fila.id.toString()
        is cl.duocuc.induztek.model.Instalacion -> fila.id.toString()
        is cl.duocuc.induztek.model.Equipo -> fila.id.toString()
        is cl.duocuc.induztek.model.Visita -> fila.id.toString()
        is cl.duocuc.induztek.model.Protocolo -> fila.id.toString()
        else -> error("Tipo de registro no admitido")
    }
}
