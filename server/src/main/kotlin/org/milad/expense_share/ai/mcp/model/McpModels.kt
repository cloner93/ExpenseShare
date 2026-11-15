package org.milad.expense_share.ai.mcp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

@Serializable
data class McpContent(
    val type: String = "text",
    val text: String
)

@Serializable
data class ActionDraftPayload(
    val draftId: String,
    val actionType: String,
    val summary: String,
    val requiresConfirmation: Boolean = true,
    val payload: JsonObject
)

@Serializable
data class McpToolResult(
    val isError: Boolean = false,
    val content: List<McpContent> = emptyList(),
    val draftAction: ActionDraftPayload? = null
) {
    companion object {
        fun success(text: String, draftAction: ActionDraftPayload? = null): McpToolResult =
            McpToolResult(
                isError = false,
                content = listOf(McpContent(type = "text", text = text)),
                draftAction = draftAction
            )

        fun error(message: String): McpToolResult =
            McpToolResult(
                isError = true,
                content = listOf(McpContent(type = "text", text = message)),
                draftAction = null
            )
    }
}

data class AgentSecurityContext(
    val userId: Int
)
