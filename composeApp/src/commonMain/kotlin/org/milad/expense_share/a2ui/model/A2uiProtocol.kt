package org.milad.expense_share.a2ui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class A2uiMessageEnvelope(
    val version: String = "v0.9",
    val createSurface: CreateSurfacePayload? = null,
    val updateComponents: UpdateComponentsPayload? = null,
    val updateDataModel: UpdateDataModelPayload? = null,
    val deleteSurface: DeleteSurfacePayload? = null,
)

@Serializable
data class CreateSurfacePayload(
    val surfaceId: String,
    val catalogId: String = "https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json",
    val theme: Map<String, String> = emptyMap(),
    val sendDataModel: Boolean = false,
)

@Serializable
data class UpdateComponentsPayload(
    val surfaceId: String,
    val components: List<A2uiComponentPayload>,
)

@Serializable
data class A2uiComponentPayload(
    val id: String,
    val component: String,
    val properties: Map<String, JsonElement> = emptyMap(),
)

@Serializable
data class UpdateDataModelPayload(
    val surfaceId: String,
    val path: String = "/",
    val value: JsonElement? = null,
)

@Serializable
data class DeleteSurfacePayload(
    val surfaceId: String,
)

@Serializable
data class A2uiClientEventRequest(
    val type: String = "click",
    val surfaceId: String,
    val componentId: String,
    val timestamp: Long = 0L,
    val context: Map<String, JsonElement> = emptyMap(),
    val clientDataModel: JsonElement? = null,
)

@Serializable
data class AgentInteractionRequest(
    val prompt: String? = null,
    val clientEvent: A2uiClientEventRequest? = null,
)

@Serializable
data class AgentScenario(
    val id: String,
    val title: String,
    val prompt: String,
    val icon: String,
)
