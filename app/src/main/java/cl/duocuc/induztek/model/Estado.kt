package cl.duocuc.induztek.model

enum class Estado(val texto: String) {
    BORRADOR("Borrador"),
    REGISTRADO("Registrado en terreno"),
    OBSERVADO("Observado"),
    FORMALIZADO("Formalizado"),
    ANULADO("Anulado"),
}
