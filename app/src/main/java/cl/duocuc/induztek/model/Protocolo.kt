package cl.duocuc.induztek.model

import java.time.LocalDate
import java.util.UUID

data class Protocolo(
    val id: Long = 0L,
    val numero: String = "PT-${LocalDate.now().year}-${UUID.randomUUID()}",
    val visitaId: Long,
    val equipoId: Long,
    val creadorId: Long,
    val fecha: String = ahora(),
    val temperatura: Double,
    val humedad: Double,
    val estado: Estado = Estado.BORRADOR,
    val mediciones: List<Medicion> = emptyList(),
    val mantenimiento: String = "",
    val observaciones: String = "",
    val historial: List<Cambio> = emptyList(),
    val reemplazaId: Long = 0L,
    val evidencia: ByteArray? = null,
)
