package cl.duocuc.induztek.model

data class Instrumento(
    val id: Long = 0L,
    val nombre: String,
    val marca: String,
    val modelo: String,
    val serie: String,
    val calibraciones: List<Calibracion>,
) {
    fun vigente(fecha: String) = calibraciones.any { fecha >= it.desde && fecha <= it.hasta }
}
