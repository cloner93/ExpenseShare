package org.milad.expense_share.ai.agent

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.milad.expense_share.ai.mcp.McpServer
import org.milad.expense_share.ai.mcp.model.AgentSecurityContext
import org.milad.expense_share.domain.repository.ConversationRepository

class AgentOrchestrator(
    private val mcpServer: McpServer,
    private val agentClient: AgentClient,
    private val conversationRepository: ConversationRepository
) {

    suspend fun handleUserMessage(
        sessionId: String,
        userText: String,
        context: AgentSecurityContext,
        imageBase64: String? = null,
        onEvent: suspend (AgentServerEvent) -> Unit
    ) {
        conversationRepository.ensureSession(sessionId, context.userId)
        conversationRepository.saveMessage(sessionId, "user", userText)

        onEvent(AgentServerEvent.Thinking("در حال تحلیل پیام..."))

        val rawHistory = conversationRepository.getMessages(sessionId).map {
            AgentMessage(role = it.role, content = it.content)
        }
        val history = if (!imageBase64.isNullOrBlank() && rawHistory.isNotEmpty()) {
            rawHistory.dropLast(1) + rawHistory.last().copy(imageBase64 = imageBase64)
        } else {
            rawHistory
        }

        val tools = mcpServer.listTools()
        val agentResponse = agentClient.generateResponse(history, tools)

        if (agentResponse.toolCalls.isNotEmpty()) {
            val textSummaries = mutableListOf<String>()
            var draftActionCard: JsonObject? = null
            var draftActionId: String? = null

            for (toolCall in agentResponse.toolCalls) {
                onEvent(AgentServerEvent.Thinking("در حال اجرای ابزار: ${toolCall.name}..."))
                val toolResult = mcpServer.executeTool(toolCall.name, toolCall.arguments, context)

                if (toolResult.draftAction != null) {
                    val draft = toolResult.draftAction
                    val cardData = buildJsonObject {
                        put("actionType", draft.actionType)
                        put("summary", draft.summary)
                        put("requiresConfirmation", draft.requiresConfirmation)
                        put("payload", draft.payload)
                    }
                    draftActionCard = cardData
                    draftActionId = draft.draftId

                    onEvent(
                        AgentServerEvent.GenerativeCard(
                            cardType = "ACTION_DRAFT",
                            draftId = draft.draftId,
                            data = cardData
                        )
                    )
                }

                if (!toolResult.isError) {
                    val summaryText = toolResult.content.firstOrNull()?.text ?: ""
                    if (summaryText.isNotBlank()) {
                        textSummaries.add(summaryText)
                    }
                } else {
                    val err = toolResult.content.firstOrNull()?.text ?: "خطا در پردازش ابزار"
                    onEvent(AgentServerEvent.Error(err))
                }
            }

            if (draftActionCard != null) {
                conversationRepository.saveMessage(
                    sessionId = sessionId,
                    role = "agent",
                    content = agentResponse.text.ifBlank { "پیش‌نویس اقدام برای تایید آماده شد." },
                    a2uiPayload = draftActionCard.toString()
                )
            }

            if (textSummaries.isNotEmpty()) {
                val fullSummary = textSummaries.joinToString("\n\n")
                onEvent(AgentServerEvent.MessageChunk(fullSummary, isFinal = true))
                conversationRepository.saveMessage(sessionId, "agent", fullSummary)
            }
            return
        }

        onEvent(AgentServerEvent.MessageChunk(agentResponse.text, isFinal = true))
        conversationRepository.saveMessage(sessionId, "agent", agentResponse.text)
    }

    suspend fun handleDraftConfirmation(
        sessionId: String,
        draftId: String,
        actionType: String,
        payload: JsonObject,
        context: AgentSecurityContext,
        onEvent: suspend (AgentServerEvent) -> Unit
    ) {
        onEvent(AgentServerEvent.Thinking("در حال نهایی‌سازی اقدام تایید شده..."))

        val toolToExecute = when (actionType) {
            "CREATE_TRANSACTION" -> "execute_expense_draft"
            "RECORD_SETTLEMENT", "PAY_SETTLEMENT" -> "execute_settlement_draft"
            "CREATE_GROUP" -> "execute_create_group_draft"
            else -> null
        }

        if (toolToExecute == null) {
            onEvent(AgentServerEvent.ActionExecuted(draftId, success = false, message = "نوع اقدام نامعتبر است."))
            return
        }

        val arguments = buildJsonObject {
            put("draftId", draftId)
            payload.forEach { (key, value) -> put(key, value) }
        }

        val result = mcpServer.executeTool(toolToExecute, arguments, context)
        val success = !result.isError
        val message = result.content.firstOrNull()?.text ?: if (success) "عملیات با موفقیت انجام شد." else "خطا در اجرا."

        onEvent(AgentServerEvent.ActionExecuted(draftId, success = success, message = message))

        conversationRepository.ensureSession(sessionId, context.userId)
        conversationRepository.saveMessage(
            sessionId = sessionId,
            role = "agent",
            content = "نتیجه اقدام: $message"
        )
    }
}
