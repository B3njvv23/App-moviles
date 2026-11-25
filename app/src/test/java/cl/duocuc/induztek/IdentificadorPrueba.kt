package cl.duocuc.induztek
// Aquí pruebo las reglas de IdentificadorPrueba.

private val secuenciaPrueba = java.util.concurrent.atomic.AtomicLong(10000L)

fun nuevoIdPrueba(): Long = secuenciaPrueba.incrementAndGet()

fun id(): String = java.util.UUID.randomUUID().toString()
