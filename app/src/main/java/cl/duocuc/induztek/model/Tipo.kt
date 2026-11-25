package cl.duocuc.induztek.model
// Aquí guardo los datos de Tipo.

enum class Tipo(val texto: String) {
    TRANSFORMADOR("Transformador"),
    INTERRUPTOR("Interruptor"),
    CELDA("Celda MT"),
    CABLE("Cable de poder"),
    RELE("Relé de protección");

    val clasificacion
        get() = if (this == RELE) "Secundario" else "Primario"
}
