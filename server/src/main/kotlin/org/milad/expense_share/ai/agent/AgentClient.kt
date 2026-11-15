package org.milad.expense_share.ai.agent

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.*
import org.milad.expense_share.ai.mcp.model.McpTool
import java.util.UUID

interface AgentClient {
    suspend fun generateResponse(
        messages: List<AgentMessage>,
        tools: List<McpTool>
    ): AgentResponse
}

class GeminiAgentClient(
    private val apiKey: String = System.getenv("GEMINI_API_KEY") ?: "",
    private val model: String = System.getenv("GEMINI_MODEL") ?: "gemini-1.5-flash"
) : AgentClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    override suspend fun generateResponse(
        messages: List<AgentMessage>,
        tools: List<McpTool>
    ): AgentResponse {
        if (apiKey.isBlank()) {
            return fallbackIntentAgent(messages, tools)
        }

        return try {
            callGeminiApi(messages, tools)
        } catch (e: Exception) {
            fallbackIntentAgent(messages, tools, errorNote = "Gemini API unavailable: ${e.message}")
        }
    }

    private suspend fun callGeminiApi(
        messages: List<AgentMessage>,
        tools: List<McpTool>
    ): AgentResponse {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contentsJson = messages.map { msg ->
            val role = if (msg.role == "assistant") "model" else "user"
            buildJsonObject {
                put("role", role)
                put("parts", buildJsonArray {
                    add(buildJsonObject { put("text", msg.content) })
                })
            }
        }

        val requestBody = buildJsonObject {
            put("contents", JsonArray(contentsJson))
        }

        val responseString = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body<String>()

        val root = json.parseToJsonElement(responseString).jsonObject
        val candidate = root["candidates"]?.jsonArray?.firstOrNull()?.jsonObject
        val text = candidate?.get("content")?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""

        return AgentResponse(text = text)
    }

    /**
     * Fallback local agent when no external LLM API key is configured.
     * Parses user natural language intent directly and delegates to MCP tools.
     */
    private fun fallbackIntentAgent(
        messages: List<AgentMessage>,
        tools: List<McpTool>,
        errorNote: String? = null
    ): AgentResponse {
        val lastUserMessage = messages.lastOrNull { it.role == "user" }?.content?.trim() ?: ""
        val lower = lastUserMessage.lowercase()

        // 1. Overview intent
        if (lower.contains("خلاصه") || lower.contains("گروه") && (lower.contains("چی دارم") || lower.contains("وضعیت"))) {
            return AgentResponse(
                text = "در حال دریافت خلاصه گروه‌ها و وضعیت مالی شما...",
                toolCalls = listOf(
                    AgentToolCall(
                        id = UUID.randomUUID().toString(),
                        name = "get_user_overview",
                        arguments = buildJsonObject {}
                    )
                )
            )
        }

        // 2. Friends intent
        if (lower.contains("دوست") || lower.contains("مخاطب")) {
            return AgentResponse(
                text = "در حال بارگذاری فهرست دوستان شما...",
                toolCalls = listOf(
                    AgentToolCall(
                        id = UUID.randomUUID().toString(),
                        name = "list_friends",
                        arguments = buildJsonObject {}
                    )
                )
            )
        }

        // 3. Propose expense intent (detect numbers or keywords like خرج, هزینه, تومان, پول)
        val numberRegex = "\\b(\\d+)\\b".toRegex()
        val numbers = numberRegex.findAll(lastUserMessage).map { it.value.toLong() }.toList()

        if ((lower.contains("خرج") || lower.contains("هزینه") || lower.contains("پول") || lower.contains("دنگ")) && numbers.isNotEmpty()) {
            val amount = numbers.first()
            return AgentResponse(
                text = "پیش‌نویس هزینه بر اساس پیام شما ایجاد شد. لطفاً مشخصات را بازبینی کرده و دکمه تایید را لمس نمایید.",
                toolCalls = listOf(
                    AgentToolCall(
                        id = UUID.randomUUID().toString(),
                        name = "propose_expense",
                        arguments = buildJsonObject {
                            put("groupId", 1) // default group fallback
                            put("title", "هزینه ثبت شده توسط ایجنت")
                            put("amount", amount)
                        }
                    )
                )
            )
        }

        return AgentResponse(
            text = "سلام! من دستیار هوشمند شما در مدیریت هزینه‌ها هستم. می‌توانید مواردی مانند «وضعیت هزینه‌ها»، «ثبت هزینه ناهار ۵۰۰۰۰ در گروه سفر» یا «تسویه‌حساب با علی» را به زبان محاوره‌ای بفرمایید."
        )
    }
}
