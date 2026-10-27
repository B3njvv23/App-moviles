package cl.duocuc.induztek.model

data class Equipo(
    val id: Long = 0L,
    val instalacionId: Long,
    val tag: String,
    val tipo: Tipo,
    val marca: String,
    val modelo: String,
    val serie: String,
    val anio: Int,
    val placa: String,
    val maxContactos: Double = 0.0,
    val observaciones: String = "",
)
