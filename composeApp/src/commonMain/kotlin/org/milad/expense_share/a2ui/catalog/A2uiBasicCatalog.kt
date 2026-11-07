package org.milad.expense_share.a2ui.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.milad.expense_share.a2ui.engine.A2uiSurfaceModel
import org.milad.expense_share.a2ui.model.A2uiComponentPayload

object A2uiBasicCatalog {

    @Composable
    fun RenderComponent(
        componentId: String,
        surfaceModel: A2uiSurfaceModel,
        modifier: Modifier = Modifier,
    ) {
        val componentsMap by surfaceModel.components.collectAsState()
        val component = componentsMap[componentId] ?: return

        when (component.component) {
            "Text" -> RenderText(component, surfaceModel, modifier)
            "Card" -> RenderCard(component, surfaceModel, modifier)
            "Column" -> RenderColumn(component, surfaceModel, modifier)
            "Row" -> RenderRow(component, surfaceModel, modifier)
            "Button" -> RenderButton(component, surfaceModel, modifier)
            "Divider" -> HorizontalDivider(modifier = modifier.padding(vertical = 4.dp))
            else -> {
                // Fallback for unknown component
                Text(
                    text = "Unsupported component: ${component.component}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = modifier
                )
            }
        }
    }

    @Composable
    private fun RenderText(
        payload: A2uiComponentPayload,
        surfaceModel: A2uiSurfaceModel,
        modifier: Modifier = Modifier,
    ) {
        val rawText = (payload.properties["text"] as? JsonPrimitive)?.content ?: ""
        val resolvedText = remember(rawText, surfaceModel) {
            if (rawText.startsWith("/")) {
                surfaceModel.resolveValue(rawText) ?: rawText
            } else {
                rawText
            }
        }

        val variant = (payload.properties["variant"] as? JsonPrimitive)?.content ?: "Body"
        val colorHex = (payload.properties["color"] as? JsonPrimitive)?.content
        val customColor = colorHex?.let { parseHexColor(it) }

        val style = when (variant) {
            "H1" -> MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            "H2" -> MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            "H3" -> MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            "Caption" -> MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium)
            else -> MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        }

        val textColor = customColor ?: when (variant) {
            "H1", "H2", "H3" -> MaterialTheme.colorScheme.onSurface
            "Caption" -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        }

        Text(
            text = resolvedText,
            style = style,
            color = textColor,
            modifier = modifier
        )
    }

    @Composable
    private fun RenderCard(
        payload: A2uiComponentPayload,
        surfaceModel: A2uiSurfaceModel,
        modifier: Modifier = Modifier,
    ) {
        val childId = (payload.properties["child"] as? JsonPrimitive)?.content
        val colorHex = (payload.properties["containerColor"] as? JsonPrimitive)?.content
        val containerColor = colorHex?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant

        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = containerColor
            )
        ) {
            if (childId != null) {
                RenderComponent(
                    componentId = childId,
                    surfaceModel = surfaceModel,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }

    @Composable
    private fun RenderColumn(
        payload: A2uiComponentPayload,
        surfaceModel: A2uiSurfaceModel,
        modifier: Modifier = Modifier,
    ) {
        val childrenArray = payload.properties["children"] as? JsonArray ?: return
        val spacing = (payload.properties["spacing"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 8

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.dp)
        ) {
            for (element in childrenArray) {
                val childId = (element as? JsonPrimitive)?.content ?: continue
                RenderComponent(
                    componentId = childId,
                    surfaceModel = surfaceModel
                )
            }
        }
    }

    @Composable
    private fun RenderRow(
        payload: A2uiComponentPayload,
        surfaceModel: A2uiSurfaceModel,
        modifier: Modifier = Modifier,
    ) {
        val childrenArray = payload.properties["children"] as? JsonArray ?: return
        val spacing = (payload.properties["spacing"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 8

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (element in childrenArray) {
                val childId = (element as? JsonPrimitive)?.content ?: continue
                RenderComponent(
                    componentId = childId,
                    surfaceModel = surfaceModel
                )
            }
        }
    }

    @Composable
    private fun RenderButton(
        payload: A2uiComponentPayload,
        surfaceModel: A2uiSurfaceModel,
        modifier: Modifier = Modifier,
    ) {
        val text = (payload.properties["text"] as? JsonPrimitive)?.content ?: "Button"
        val variant = (payload.properties["variant"] as? JsonPrimitive)?.content ?: "Filled"
        val action = payload.properties["action"] as? JsonObject ?: JsonObject(emptyMap())

        val onClick = {
            surfaceModel.dispatchAction(action, payload.id)
        }

        when (variant) {
            "Outlined" -> {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = text, fontSize = 13.sp)
                }
            }
            "Text" -> {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = text, fontSize = 13.sp)
                }
            }
            else -> {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = text, fontSize = 13.sp)
                }
            }
        }
    }

    private fun parseHexColor(hex: String): Color {
        val cleaned = hex.removePrefix("#")
        return try {
            val longVal = cleaned.toLong(16)
            if (cleaned.length == 6) {
                Color(0xFF000000 or longVal)
            } else {
                Color(longVal)
            }
        } catch (_: Exception) {
            Color.Unspecified
        }
    }
}
