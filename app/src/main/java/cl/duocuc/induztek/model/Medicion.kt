package cl.duocuc.induztek.model

data class Medicion(
    val id: Long = 0L,
    val prueba: Prueba,
    val elemento: String,
    val momento: String = "Única",
    val realizada: Boolean = true,
    val motivo: String = "",
    val instrumentoId: Long = 0L,
    val valores: Map<String, Double> = emptyMap(),
    val resultado: Resultado? = null,
    val comentario: String = "",
)
