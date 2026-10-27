package cl.duocuc.induztek.model

data class Usuario(
    val id: Long = 0L,
    val nombre: String,
    val rut: String,
    val correo: String,
    val rol: Rol,
    val clienteId: Long = 0L,
    val activo: Boolean = true,
    val hash: String = "",
    val sal: String = "",
)
