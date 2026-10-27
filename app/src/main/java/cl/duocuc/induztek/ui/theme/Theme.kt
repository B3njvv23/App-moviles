package cl.duocuc.induztek.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import cl.duocuc.induztek.R

private val EsquemaClaro =
    lightColorScheme(
        primary = Primario,
        onPrimary = Blanco,
        primaryContainer = PrimarioClaro,
        onPrimaryContainer = PrimarioOscuro,
        secondary = Secundario,
        onSecondary = Blanco,
        tertiary = Acento,
        onTertiary = Blanco,
        background = FondoClaro,
        onBackground = TextoOscuro,
        surface = SuperficieClara,
        onSurface = TextoOscuro,
    )

private val EsquemaOscuro =
    darkColorScheme(
        primary = PrimarioClaro,
        onPrimary = PrimarioOscuro,
        primaryContainer = PrimarioOscuro,
        onPrimaryContainer = PrimarioClaro,
        secondary = Secundario,
        onSecondary = Blanco,
        tertiary = Acento,
        onTertiary = Blanco,
        background = FondoOscuro,
        onBackground = TextoClaro,
        surface = SuperficieOscura,
        onSurface = TextoClaro,
    )

private val Formas =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
    )

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme =
            (if (darkTheme) EsquemaOscuro else EsquemaClaro).copy(
                primary = colorResource(R.color.induztek_primary),
                onPrimary = colorResource(R.color.induztek_on_primary),
                primaryContainer = colorResource(R.color.induztek_primary_container),
                onPrimaryContainer = colorResource(R.color.induztek_on_primary_container),
                secondary = colorResource(R.color.induztek_secondary),
                tertiary = colorResource(R.color.induztek_accent),
                background = colorResource(R.color.induztek_background),
                surface = colorResource(R.color.induztek_surface),
                onBackground = colorResource(R.color.induztek_text),
                onSurface = colorResource(R.color.induztek_text),
            ),
        typography = Tipografia,
        shapes = Formas,
        content = content,
    )
}
