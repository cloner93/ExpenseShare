package org.milad.expense_share.a2ui.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.milad.expense_share.a2ui.model.A2uiClientEventRequest
import org.milad.expense_share.a2ui.model.A2uiComponentPayload

class A2uiSurfaceModel(
    val surfaceId: String,
    val catalogId: String,
    val theme: Map<String, String> = emptyMap(),
    private val onDispatchAction: (A2uiClientEventRequest) -> Unit,
) {
    private val _components = MutableStateFlow<Map<String, A2uiComponentPayload>>(emptyMap())
    val components: StateFlow<Map<String, A2uiComponentPayload>> = _components.asStateFlow()

    private val _dataModel = MutableStateFlow<Map<String, JsonElement>>(emptyMap())
    val dataModel: StateFlow<Map<String, JsonElement>> = _dataModel.asStateFlow()

    val rootComponentId: String = "root"

    fun updateComponents(newComponents: List<A2uiComponentPayload>) {
        val current = _components.value.toMutableMap()
        for (comp in newComponents) {
            current[comp.id] = comp
        }
        _components.value = current
    }

    fun updateDataModel(path: String, value: JsonElement?) {
        if (value == null) return
        val current = _dataModel.value.toMutableMap()
        if (path == "/" && value is JsonObject) {
            current.putAll(value)
        } else {
            val key = path.removePrefix("/")
            current[key] = value
        }
        _dataModel.value = current
    }

    fun dispatchAction(actionContext: Map<String, JsonElement>, componentId: String) {
        val event = A2uiClientEventRequest(
            type = "click",
            surfaceId = surfaceId,
            componentId = componentId,
            timestamp = 0L,
            context = actionContext,
            clientDataModel = JsonObject(_dataModel.value)
        )
        onDispatchAction(event)
    }

    fun resolveValue(keyOrPath: String): String? {
        val key = keyOrPath.removePrefix("/")
        val elem = _dataModel.value[key] ?: return null
        return (elem as? JsonPrimitive)?.content ?: elem.toString()
    }
}
