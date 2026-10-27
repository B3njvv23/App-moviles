package cl.duocuc.induztek.model

data class Campo(
    val clave: String,
    val etiqueta: String,
    val opciones: List<Pair<String, String>> = emptyList(),
    val obligatorio: Boolean = true,
    val numero: Boolean = false,
    val inicial: String = "",
    val selector: Boolean = false,
)
