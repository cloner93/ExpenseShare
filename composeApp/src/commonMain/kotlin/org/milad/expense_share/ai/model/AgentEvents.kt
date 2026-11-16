package org.milad.expense_share.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class AgentClientMessage {
    @Serializable
    @SerialName("SendMessage")
    data class SendMessage(
        val sessionId: String,
        val text: String,
        val imageBase64: String? = null
    ) : AgentClientMessage()

    @Serializable
    @SerialName("ConfirmDraft")
    data class ConfirmDraft(
        val sessionId: String,
        val draftId: String,
        val actionType: String,
        val payload: JsonObject
    ) : AgentClientMessage()

    @Serializable
    @SerialName("CancelDraft")
    data class CancelDraft(
        val sessionId: String,
        val draftId: String
    ) : AgentClientMessage()
}

sealed class AgentServerMessage {
    data class Thinking(val status: String) : AgentServerMessage()

    data class MessageChunk(val text: String, val isFinal: Boolean = false) : AgentServerMessage()

    data class GenerativeCard(
        val cardType: String,
        val draftId: String? = null,
        val data: JsonObject
    ) : AgentServerMessage()

    data class ActionExecuted(
        val draftId: String,
        val success: Boolean,
        val message: String
    ) : AgentServerMessage()

    data class Error(val message: String) : AgentServerMessage()
}
