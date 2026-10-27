package cl.duocuc.induztek.model

data class Formulario(
    val titulo: String,
    val tipo: String,
    val ref: String,
    val extra: String,
    val campos: List<Campo>,
    val valores: Map<String, String>,
    val errores: Map<String, String> = emptyMap(),
)
