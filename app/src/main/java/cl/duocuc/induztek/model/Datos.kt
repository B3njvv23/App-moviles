package cl.duocuc.induztek.model

data class Datos(
    val usuarios: List<Usuario> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val instalaciones: List<Instalacion> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val instrumentos: List<Instrumento> = emptyList(),
    val visitas: List<Visita> = emptyList(),
    val protocolos: List<Protocolo> = emptyList(),
)
