package cl.duocuc.induztek.model
// Aquí guardo los datos de Formulario.

data class Formulario(
    val titulo: String,
    val tipo: String,
    val ref: String,
    val extra: String,
    val campos: List<Campo>,
    val valores: Map<String, String>,
    val errores: Map<String, String> = emptyMap(),
)
