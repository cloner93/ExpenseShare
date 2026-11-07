package org.milad.expense_share.a2ui.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.milad.expense_share.a2ui.catalog.A2uiBasicCatalog
import org.milad.expense_share.a2ui.engine.A2uiSurfaceModel

@Composable
fun A2uiSurface(
    surfaceModel: A2uiSurfaceModel,
    modifier: Modifier = Modifier,
) {
    val components by surfaceModel.components.collectAsState()
    val hasRoot = components.containsKey(surfaceModel.rootComponentId)

    AnimatedContent(
        targetState = hasRoot,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "A2uiSurfaceAnimation",
        modifier = modifier
    ) { ready ->
        if (ready) {
            A2uiBasicCatalog.RenderComponent(
                componentId = surfaceModel.rootComponentId,
                surfaceModel = surfaceModel
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
