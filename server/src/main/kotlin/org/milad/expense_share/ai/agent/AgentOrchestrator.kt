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
        onEvent: suspend (AgentServerEvent) -> Unit
    ) {
        conversationRepository.ensureSession(sessionId, context.userId)
        conversationRepository.saveMessage(sessionId, "user", userText)

        onEvent(AgentServerEvent.Thinking("در حال تحلیل پیام..."))

        val history = conversationRepository.getMessages(sessionId).map {
            AgentMessage(role = it.role, content = it.content)
        }

        val tools = mcpServer.listTools()
        val agentResponse = agentClient.generateResponse(history, tools)

        if (agentResponse.toolCalls.isNotEmpty()) {
            for (toolCall in agentResponse.toolCalls) {
                onEvent(AgentServerEvent.Thinking("در حال فراخوانی ابزار: ${toolCall.name}..."))
                val toolResult = mcpServer.executeTool(toolCall.name, toolCall.arguments, context)

                if (toolResult.draftAction != null) {
                    val draft = toolResult.draftAction
                    val cardData = buildJsonObject {
                        put("actionType", draft.actionType)
                        put("summary", draft.summary)
                        put("requiresConfirmation", draft.requiresConfirmation)
                        put("payload", draft.payload)
                    }

                    onEvent(
                        AgentServerEvent.GenerativeCard(
                            cardType = "ACTION_DRAFT",
                            draftId = draft.draftId,
                            data = cardData
                        )
                    )

                    conversationRepository.saveMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = agentResponse.text,
                        a2uiPayload = cardData.toString()
                    )
                    return
                }

                if (!toolResult.isError) {
                    val summaryText = toolResult.content.firstOrNull()?.text ?: ""
                    onEvent(AgentServerEvent.MessageChunk(summaryText, isFinal = true))
                    conversationRepository.saveMessage(sessionId, "assistant", summaryText)
                    return
                } else {
                    val err = toolResult.content.firstOrNull()?.text ?: "خطا در پردازش"
                    onEvent(AgentServerEvent.Error(err))
                    return
                }
            }
        }

        onEvent(AgentServerEvent.MessageChunk(agentResponse.text, isFinal = true))
        conversationRepository.saveMessage(sessionId, "assistant", agentResponse.text)
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
            "PAY_SETTLEMENT" -> "execute_settlement_draft"
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
            role = "assistant",
            content = "نتیجه اقدام: $message"
        )
    }
}
