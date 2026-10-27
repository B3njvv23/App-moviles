package cl.duocuc.induztek.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*
import cl.duocuc.induztek.viewmodel.Reglas

@Composable
fun TarjetaProtocolo(p: Protocolo, d: Datos, cliente: Boolean, click: () -> Unit) {
    TarjetaSeccion(
        d.equipos.firstOrNull { it.id == p.equipoId }?.tag ?: "Equipo",
        Modifier.clickable(onClick = click).animateContentSize(),
    ) {
        Text(p.numero, style = MaterialTheme.typography.labelMedium)
        Text(
            "${p.fecha.take(10)} · ${d.usuarios.firstOrNull { it.id == p.creadorId }?.nombre.orEmpty()}",
            style = MaterialTheme.typography.bodySmall,
        )
        Etiqueta(
            if (cliente && p.estado != Estado.FORMALIZADO) "Pendiente de formalización"
            else p.estado.texto
        )
        Text("Resultado: ${Reglas.global(p)?.texto ?: "Pendiente de revisión"}")
        if (Reglas.atrasado(p) && !cliente)
            Text("Supera meta de 2 días hábiles", color = MaterialTheme.colorScheme.error)
        TextButton(onClick = click) { Text("Ver protocolo") }
    }
}
