package org.milad.expense_share.ai.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AgentMessage(
    val role: String,
    val content: String,
    val toolCallId: String? = null,
    val toolName: String? = null
)

@Serializable
data class AgentToolCall(
    val id: String,
    val name: String,
    val arguments: JsonObject
)

@Serializable
data class AgentResponse(
    val text: String,
    val toolCalls: List<AgentToolCall> = emptyList()
)

@Serializable
sealed class AgentClientEvent {
    @Serializable
    data class SendMessage(
        val sessionId: String,
        val text: String,
        val imageBase64: String? = null
    ) : AgentClientEvent()

    @Serializable
    data class ConfirmDraft(
        val sessionId: String,
        val draftId: String,
        val actionType: String,
        val payload: JsonObject
    ) : AgentClientEvent()

    @Serializable
    data class CancelDraft(
        val sessionId: String,
        val draftId: String
    ) : AgentClientEvent()
}

@Serializable
sealed class AgentServerEvent {
    @Serializable
    data class Thinking(val status: String) : AgentServerEvent()

    @Serializable
    data class MessageChunk(val text: String, val isFinal: Boolean = false) : AgentServerEvent()

    @Serializable
    data class GenerativeCard(
        val cardType: String,
        val draftId: String?,
        val data: JsonObject
    ) : AgentServerEvent()

    @Serializable
    data class ActionExecuted(
        val draftId: String,
        val success: Boolean,
        val message: String
    ) : AgentServerEvent()

    @Serializable
    data class Error(val message: String) : AgentServerEvent()
}
