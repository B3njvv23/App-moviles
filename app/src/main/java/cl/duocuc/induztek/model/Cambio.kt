package cl.duocuc.induztek.model
// Aquí guardo los datos de Cambio.

data class Cambio(
    val usuarioId: Long,
    val estado: Estado,
    val fecha: String = ahora(),
    val motivo: String = "",
)
