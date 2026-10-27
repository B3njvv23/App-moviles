package cl.duocuc.induztek.model

data class Recepcion(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val fecha: String = ahora(),
)
