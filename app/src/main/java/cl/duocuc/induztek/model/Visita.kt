package cl.duocuc.induztek.model

import java.time.LocalDate

data class Visita(
    val id: Long = 0L,
    val instalacionId: Long,
    val ot: String,
    val fecha: String = LocalDate.now().toString(),
    val ingenieroId: Long,
    val participantes: List<Long>,
    val mantenimiento: Boolean = false,
    val cierre: String = "",
    val recepcion: Recepcion? = null,
    val acta: List<String> = emptyList(),
)
