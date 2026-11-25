package cl.duocuc.induztek.model
// Aquí guardo los datos de Recepcion.

data class Recepcion(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val fecha: String = ahora(),
)
