package cl.duocuc.induztek.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import cl.duocuc.induztek.R
import cl.duocuc.induztek.model.*
import cl.duocuc.induztek.ui.components.*
import cl.duocuc.induztek.viewmodel.*

@Composable
fun Contenido(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = dimensionResource(R.dimen.content_max_width))
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.screen_padding))
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.content_spacing)),
            content = content,
        )
    }
}
