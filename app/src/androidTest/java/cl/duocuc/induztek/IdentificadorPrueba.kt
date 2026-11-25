package cl.duocuc.induztek
// Aquí pruebo el flujo de IdentificadorPrueba en Android.

private val secuenciaPrueba = java.util.concurrent.atomic.AtomicLong(10000L)

fun nuevoIdPrueba(): Long = secuenciaPrueba.incrementAndGet()

fun id(): String = java.util.UUID.randomUUID().toString()
