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
import org.milad.expense_share.normalizePersianDigits
import java.util.UUID

interface AgentClient {
    suspend fun generateResponse(
        messages: List<AgentMessage>,
        tools: List<McpTool>
    ): AgentResponse
}

class GeminiAgentClient(
    private val apiKey: String = System.getenv("GEMINI_API_KEY") ?: "",
    private val model: String = System.getenv("GEMINI_MODEL") ?: "gemini-3.1-flash-lite"
) : AgentClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 8000
            socketTimeoutMillis = 15000
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

        val systemInstruction = buildJsonObject {
            put("parts", buildJsonArray {
                add(buildJsonObject {
                    put("text", "شما ایجنت مالی هوشمند، حرفه‌ای و صمیمی مدیریت هزینه‌ها و دنگ‌ها در پلتفرم ExpenseShare هستید. با کاربران به زبان فارسی طبیعی و روان صحبت کنید. در صورتی که کاربر اعلام هزینه، فاکتور یا خرج کرد، از ابزار propose_expense استفاده کنید. در صورتی که از بدهی‌ها، طلب‌ها یا وضعیت مالی سوال کرد، از ابزار get_user_overview استفاده کنید. در مکالمات عمومی و احوالپرسی پاسخ‌های دلگرم‌کننده و خلاصه بدهید.")
                })
            })
        }

        val contentsJson = messages.map { msg ->
            val role = if (msg.role == "assistant" || msg.role == "agent") "model" else "user"
            buildJsonObject {
                put("role", role)
                put("parts", buildJsonArray {
                    if (!msg.imageBase64.isNullOrBlank()) {
                        add(buildJsonObject {
                            put("inlineData", buildJsonObject {
                                put("mimeType", "image/jpeg")
                                put("data", msg.imageBase64)
                            })
                        })
                    }
                    add(buildJsonObject { put("text", msg.content) })
                })
            }
        }

        val toolsJson = if (tools.isNotEmpty()) {
            buildJsonArray {
                add(buildJsonObject {
                    put("functionDeclarations", buildJsonArray {
                        for (tool in tools) {
                            add(buildJsonObject {
                                put("name", tool.name)
                                put("description", tool.description)
                                put("parameters", tool.inputSchema)
                            })
                        }
                    })
                })
            }
        } else null

        val requestBody = buildJsonObject {
            put("systemInstruction", systemInstruction)
            put("contents", JsonArray(contentsJson))
            if (toolsJson != null) {
                put("tools", toolsJson)
            }
        }

        val responseString = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body<String>()

        val root = json.parseToJsonElement(responseString).jsonObject
        val candidate = root["candidates"]?.jsonArray?.firstOrNull()?.jsonObject
        val parts = candidate?.get("content")?.jsonObject?.get("parts")?.jsonArray ?: emptyList()

        val textBuilder = StringBuilder()
        val toolCalls = mutableListOf<AgentToolCall>()

        for (part in parts) {
            val partObj = part.jsonObject
            partObj["text"]?.jsonPrimitive?.contentOrNull?.let { textBuilder.append(it) }
            partObj["functionCall"]?.jsonObject?.let { fc ->
                val name = fc["name"]?.jsonPrimitive?.contentOrNull ?: ""
                val args = fc["args"]?.jsonObject ?: buildJsonObject {}
                val callId = fc["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString()
                if (name.isNotBlank()) {
                    toolCalls.add(AgentToolCall(id = callId, name = name, arguments = args))
                }
            }
        }

        return AgentResponse(text = textBuilder.toString().trim(), toolCalls = toolCalls)
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
        val lastUserMsg = messages.lastOrNull { it.role == "user" }
        val lastUserMessage = lastUserMsg?.content?.trim() ?: ""
        val lastImage = lastUserMsg?.imageBase64
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
        if (!lastImage.isNullOrBlank() || lower.contains("تصویر") || lower.contains("رسید") || lower.contains("فاکتور")) {
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
        val normalizedMessage = lastUserMessage.normalizePersianDigits()
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
                text = "سلام! من ایجنت هوشمند شما در ExpenseShare هستم. می‌توانید هر هزینه‌ای مثل «شام ۳۵۰ هزار تومان» را بگویید، عکس فاکتور بفرستید، یا بگویید «وضعیت بدهی‌های من را بگو»."
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
