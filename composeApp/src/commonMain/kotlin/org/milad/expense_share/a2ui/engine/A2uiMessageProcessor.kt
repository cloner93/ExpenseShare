package org.milad.expense_share.a2ui.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.milad.expense_share.a2ui.model.A2uiClientEventRequest
import org.milad.expense_share.a2ui.model.A2uiMessageEnvelope

class A2uiMessageProcessor(
    private val scope: CoroutineScope,
) {
    private val _surfacesMap = LinkedHashMap<String, A2uiSurfaceModel>()
    private val _activeSurfaces = MutableStateFlow<List<A2uiSurfaceModel>>(emptyList())
    val activeSurfaces: StateFlow<List<A2uiSurfaceModel>> = _activeSurfaces.asStateFlow()

    private val _outboundEvents = MutableSharedFlow<A2uiClientEventRequest>(extraBufferCapacity = 64)
    val outboundEvents: SharedFlow<A2uiClientEventRequest> = _outboundEvents.asSharedFlow()

    fun processEnvelopes(envelopes: List<A2uiMessageEnvelope>) {
        for (envelope in envelopes) {
            processEnvelope(envelope)
        }
    }

    fun processEnvelope(envelope: A2uiMessageEnvelope) {
        val create = envelope.createSurface
        if (create != null) {
            val surface = A2uiSurfaceModel(
                surfaceId = create.surfaceId,
                catalogId = create.catalogId,
                theme = create.theme,
                onDispatchAction = { event ->
                    scope.launch {
                        _outboundEvents.emit(event)
                    }
                }
            )
            _surfacesMap[create.surfaceId] = surface
            _activeSurfaces.value = _surfacesMap.values.toList()
        }

        val updateData = envelope.updateDataModel
        if (updateData != null) {
            val surface = _surfacesMap[updateData.surfaceId]
            surface?.updateDataModel(updateData.path, updateData.value)
        }

        val updateComps = envelope.updateComponents
        if (updateComps != null) {
            val surface = _surfacesMap[updateComps.surfaceId]
            surface?.updateComponents(updateComps.components)
        }

        val delete = envelope.deleteSurface
        if (delete != null) {
            _surfacesMap.remove(delete.surfaceId)
            _activeSurfaces.value = _surfacesMap.values.toList()
        }
    }

    fun clearSurfaces() {
        _surfacesMap.clear()
        _activeSurfaces.value = emptyList()
    }
}
