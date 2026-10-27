package cl.duocuc.induztek.model

data class Cambio(
    val usuarioId: Long,
    val estado: Estado,
    val fecha: String = ahora(),
    val motivo: String = "",
)
