package cl.duocuc.induztek.model
// Aquí guardo los datos de Rol.

enum class Rol(val texto: String) {
    TECNICO("Técnico"),
    INGENIERO("Ingeniero"),
    JEFE("Jefe técnico"),
    CLIENTE("Cliente"),
}
