package cl.duocuc.induztek.model
// Aquí guardo los datos de UiState.

data class UiState(
    val datos: Datos = Datos(),
    val usuario: Usuario? = null,
    val mensaje: String = "",
    val ocupado: Boolean = false,
    val formulario: Formulario? = null,
    val guardado: Int = 0,
    val listo: Boolean = false,
    val erroresLogin: Map<String, String> = emptyMap(),
    val erroresAcceso: Map<String, String> = emptyMap(),
    val resultadoAcceso: String = "",
    val erroresPerfil: Map<String, String> = emptyMap(),
    val perfilGuardado: Int = 0,
)
