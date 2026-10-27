package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun Vacio(t: String) {
    TarjetaSeccion("Sin registros") { Text(t) }
}
