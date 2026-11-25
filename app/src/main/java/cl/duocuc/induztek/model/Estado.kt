package cl.duocuc.induztek.model
// Aquí guardo los datos de Estado.

enum class Estado(val texto: String) {
    BORRADOR("Borrador"),
    REGISTRADO("Registrado en terreno"),
    OBSERVADO("Observado"),
    FORMALIZADO("Formalizado"),
    ANULADO("Anulado"),
}
