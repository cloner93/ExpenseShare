package org.milad.expense_share.a2ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.milad.expense_share.a2ui.engine.A2uiMessageProcessor
import org.milad.expense_share.a2ui.model.AgentInteractionRequest
import org.milad.expense_share.a2ui.model.AgentScenario
import org.milad.expense_share.a2ui.network.A2uiService

class A2uiAgentViewModel(
    private val service: A2uiService = A2uiService(),
) : ViewModel() {

    val processor = A2uiMessageProcessor(viewModelScope)
    val activeSurfaces = processor.activeSurfaces

    private val _scenarios = MutableStateFlow<List<AgentScenario>>(emptyList())
    val scenarios: StateFlow<List<AgentScenario>> = _scenarios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    init {
        viewModelScope.launch {
            processor.outboundEvents.collect { clientEvent ->
                _isLoading.value = true
                try {
                    val responseEnvelopes = service.sendInteraction(
                        AgentInteractionRequest(clientEvent = clientEvent)
                    )
                    processor.processEnvelopes(responseEnvelopes)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedScenarios = service.fetchScenarios()
                _scenarios.value = loadedScenarios
                val initialPrompt = loadedScenarios.firstOrNull()?.prompt ?: "Split bill"
                val envelopes = service.sendInteraction(AgentInteractionRequest(prompt = initialPrompt))
                processor.processEnvelopes(envelopes)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onPromptInputChanged(text: String) {
        _promptInput.value = text
    }

    fun submitPrompt(prompt: String = _promptInput.value) {
        if (prompt.isBlank()) return
        _promptInput.value = ""
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val envelopes = service.sendInteraction(AgentInteractionRequest(prompt = prompt))
                processor.processEnvelopes(envelopes)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectScenario(scenario: AgentScenario) {
        submitPrompt(scenario.prompt)
    }
}
