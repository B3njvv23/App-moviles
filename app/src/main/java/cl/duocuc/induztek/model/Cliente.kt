package cl.duocuc.induztek.model

data class Cliente(
    val id: Long = 0L,
    val nombre: String,
    val rut: String,
    val giro: String,
    val contacto: String,
    val telefono: String,
    val correo: String,
)
