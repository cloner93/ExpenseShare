package org.milad.expense_share.ai.agent

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
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
    private val model: String = System.getenv("GEMINI_MODEL") ?: "gemini-2.5-flash"
) : AgentClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 6000
            connectTimeoutMillis = 4000
            socketTimeoutMillis = 6000
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
            val res = callGeminiApi(messages, tools)
            if (res.text.isBlank() && res.toolCalls.isEmpty()) {
                fallbackIntentAgent(messages, tools)
            } else {
                res
            }
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
        val normalizedMessage = lastUserMessage
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
        val numberRegex = "(\\d+)".toRegex()
        val numbers = numberRegex.findAll(normalizedMessage).mapNotNull { it.value.toLongOrNull() }.toList()

        if ((lower.contains("خرج") || lower.contains("هزینه") || lower.contains("پول") || lower.contains("دنگ")) && numbers.isNotEmpty()) {
            val amount = numbers.first()
            return AgentResponse(
                text = "پیش‌نویس هزینه بر اساس پیام شما ایجاد شد. لطفاً مشخصات را بازبینی کرده و دکمه تایید را لمس نمایید.",
                toolCalls = listOf(
                    AgentToolCall(
                        id = UUID.randomUUID().toString(),
                        name = "propose_expense",
                        arguments = buildJsonObject {
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
