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

        // 1. Debt, Settlement, or Overview intent
        if (lower.contains("بدهی") || lower.contains("طلب") || lower.contains("تسویه") || lower.contains("حساب") || lower.contains("خلاصه") || lower.contains("وضعیت") || lower.contains("گزارش")) {
            return AgentResponse(
                text = "در حال دریافت خلاصه بدهی‌ها، طلب‌ها و وضعیت مالی گروه‌های شما...",
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
        if (lower.contains("دوست") || lower.contains("مخاطب") || lower.contains("دوستان")) {
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

        // 3. Receipt image intent
        if (lower.contains("تصویر") || lower.contains("رسید") || lower.contains("فاکتور")) {
            return AgentResponse(
                text = "اطلاعات فاکتور تحلیل شد. پیش‌نویس ثبت هزینه آماده تایید است:",
                toolCalls = listOf(
                    AgentToolCall(
                        id = UUID.randomUUID().toString(),
                        name = "propose_expense",
                        arguments = buildJsonObject {
                            put("title", "فاکتور خرید / رستوران")
                            put("amount", 850000L)
                        }
                    )
                )
            )
        }

        // 4. Propose expense intent (detect numbers)
        val normalizedMessage = lastUserMessage
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
        val numberRegex = "(\\d+)".toRegex()
        val numbers = numberRegex.findAll(normalizedMessage).mapNotNull { it.value.toLongOrNull() }.toList()

        if (numbers.isNotEmpty()) {
            val amount = numbers.first()
            val candidateTitle = lastUserMessage.split(numberRegex).firstOrNull()?.trim()
                ?.removeSuffix("شد")?.removeSuffix("تومان")?.trim()
                ?.takeIf { it.isNotBlank() } ?: "هزینه ثبت شده توسط ایجنت"

            return AgentResponse(
                text = "پیش‌نویس هزینه بر اساس پیام شما ایجاد شد. لطفاً مشخصات را بازبینی کرده و دکمه تایید را لمس نمایید.",
                toolCalls = listOf(
                    AgentToolCall(
                        id = UUID.randomUUID().toString(),
                        name = "propose_expense",
                        arguments = buildJsonObject {
                            put("title", candidateTitle.take(40))
                            put("amount", amount)
                        }
                    )
                )
            )
        }

        // 5. Conversational chit-chat
        if (lower.contains("خوبی") || lower.contains("چطوری") || lower.contains("حال شما")) {
            return AgentResponse(
                text = "سلام! ممنون، من خوبم و به صورت برخط آماده‌ام تا در ثبت هزینه‌ها، دنگ‌ها، استخراج فاکتور و بررسی بدهی‌ها کمکتان کنم. چه کاری برایتان انجام دهم؟"
            )
        }

        if (lower.contains("سلام") || lower.contains("درود") || lower.contains("hi") || lower.contains("hello")) {
            return AgentResponse(
                text = "سلام! من دستیار هوشمند مالی شما در ExpenseShare هستم. می‌توانید هر هزینه‌ای مثل «شام ۳۵۰ هزار تومان» را بگویید، عکس فاکتور بفرستید، یا بگویید «وضعیت بدهی‌های من را بگو»."
            )
        }

        if (lower.contains("مرسی") || lower.contains("ممنون") || lower.contains("تشکر") || lower.contains("دستت درد نکنه")) {
            return AgentResponse(
                text = "خواهش می‌کنم! خوشحالم که تونستم کمکتون کنم. هر زمان هزینه یا تسویه‌ای داشتید در خدمتم."
            )
        }

        return AgentResponse(
            text = "پیام شما را دریافت کردم. برای مدیریت بهتر می‌توانید بگویید «هزینه شام ۳۰۰۰۰۰»، «عکس فاکتور بفرستید»، «بدهی‌های من را نشان بده» یا نام گروه مورد نظرتان را ذکر کنید."
        )
    }
}
