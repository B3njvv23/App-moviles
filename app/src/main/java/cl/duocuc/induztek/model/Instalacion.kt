package cl.duocuc.induztek.model

data class Instalacion(
    val id: Long = 0L,
    val clienteId: Long,
    val nombre: String,
    val ubicacion: String,
    val comuna: String,
    val region: String,
    val acceso: String,
)
