package org.milad.expense_share.ai.client

import client.HttpConfig
import getDefaultBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.isSecure
import io.ktor.http.takeFrom
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.milad.expense_share.ai.model.AgentClientMessage
import org.milad.expense_share.ai.model.AgentServerMessage
import token.TokenProvider

class AgentSocketClient(
    private val httpClient: HttpClient,
    private val tokenProvider: TokenProvider,
    private val httpConfig: HttpConfig = HttpConfig()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var session: DefaultClientWebSocketSession? = null
    private var listenJob: Job? = null
    private var isAutoReconnectEnabled = true

    suspend fun connect(
        onMessage: (AgentServerMessage) -> Unit,
        onConnectionChanged: (isConnected: Boolean) -> Unit
    ) {
        cleanSession()
        isAutoReconnectEnabled = true

        val token = tokenProvider.getToken()?.accessToken
        if (token.isNullOrBlank()) {
            onConnectionChanged(false)
            onMessage(AgentServerMessage.Error("کاربر احراز هویت نشده است"))
            return
        }

        try {
            val base = if (httpConfig.baseUrl.isNotBlank()) httpConfig.baseUrl else getDefaultBaseUrl()
            val urlBuilder = URLBuilder(base)
            val isHttps = urlBuilder.protocol.isSecure()

            session = httpClient.webSocketSession {
                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                url {
                    takeFrom(base.trimEnd('/'))
                    protocol = if (isHttps) URLProtocol.WSS else URLProtocol.WS
                    pathSegments = listOf("api", "agent", "chat")
                    parameters.append("token", token)
                }
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            onConnectionChanged(true)

            listenJob = scope.launch {
                try {
                    val activeSession = session ?: return@launch
                    for (frame in activeSession.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val parsed = parseServerMessage(text)
                            if (parsed != null) {
                                onMessage(parsed)
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    // Normal coroutine cancellation
                } catch (e: Exception) {
                    onMessage(AgentServerMessage.Error(e.message ?: "خطای ارتباط با سرور"))
                } finally {
                    onConnectionChanged(false)
                    if (isAutoReconnectEnabled && scope.isActive) {
                        delay(3000)
                        connect(onMessage, onConnectionChanged)
                    }
                }
            }
        } catch (e: Exception) {
            onConnectionChanged(false)
            if (isAutoReconnectEnabled && scope.isActive) {
                scope.launch {
                    delay(3000)
                    connect(onMessage, onConnectionChanged)
                }
            }
        }
    }

    suspend fun sendUserMessage(sessionId: String, text: String, imageBase64: String? = null) {
        val payload = AgentClientMessage.SendMessage(
            sessionId = sessionId,
            text = text,
            imageBase64 = imageBase64
        )
        val payloadText = json.encodeToString(AgentClientMessage.SendMessage.serializer(), payload)
        sendTextFrame(payloadText)
    }

    suspend fun confirmDraft(
        sessionId: String,
        draftId: String,
        actionType: String,
        payload: JsonObject
    ) {
        val message = AgentClientMessage.ConfirmDraft(
            sessionId = sessionId,
            draftId = draftId,
            actionType = actionType,
            payload = payload
        )
        val payloadText = json.encodeToString(AgentClientMessage.ConfirmDraft.serializer(), message)
        sendTextFrame(payloadText)
    }

    suspend fun cancelDraft(sessionId: String, draftId: String) {
        val message = AgentClientMessage.CancelDraft(
            sessionId = sessionId,
            draftId = draftId
        )
        val payloadText = json.encodeToString(AgentClientMessage.CancelDraft.serializer(), message)
        sendTextFrame(payloadText)
    }

    private suspend fun sendTextFrame(text: String) {
        val currentSession = session
        if (currentSession != null && currentSession.isActive) {
            currentSession.send(Frame.Text(text))
        } else {
            throw IllegalStateException("اتصال سوکت برقرار نیست")
        }
    }

    private suspend fun cleanSession() {
        listenJob?.cancel()
        listenJob = null
        try {
            session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client closed connection"))
        } catch (_: Exception) {}
        session = null
    }

    suspend fun disconnect() {
        isAutoReconnectEnabled = false
        cleanSession()
    }

    private fun parseServerMessage(rawText: String): AgentServerMessage? {
        return try {
            val jsonElement = json.parseToJsonElement(rawText).jsonObject

            when {
                jsonElement.containsKey("cardType") -> {
                    val cardType = jsonElement["cardType"]?.jsonPrimitive?.contentOrNull ?: "ACTION_DRAFT"
                    val draftId = jsonElement["draftId"]?.jsonPrimitive?.contentOrNull
                    val data = jsonElement["data"]?.jsonObject ?: JsonObject(emptyMap())
                    AgentServerMessage.GenerativeCard(cardType, draftId, data)
                }
                jsonElement.containsKey("status") -> {
                    val status = jsonElement["status"]?.jsonPrimitive?.contentOrNull ?: "در حال پردازش..."
                    AgentServerMessage.Thinking(status)
                }
                jsonElement.containsKey("text") -> {
                    val text = jsonElement["text"]?.jsonPrimitive?.contentOrNull ?: ""
                    val isFinal = jsonElement["isFinal"]?.jsonPrimitive?.booleanOrNull ?: false
                    AgentServerMessage.MessageChunk(text, isFinal)
                }
                jsonElement.containsKey("success") && jsonElement.containsKey("draftId") -> {
                    val draftId = jsonElement["draftId"]?.jsonPrimitive?.contentOrNull ?: ""
                    val success = jsonElement["success"]?.jsonPrimitive?.booleanOrNull ?: true
                    val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull ?: ""
                    AgentServerMessage.ActionExecuted(draftId, success, message)
                }
                jsonElement.containsKey("message") -> {
                    val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull ?: ""
                    AgentServerMessage.Error(message)
                }
                else -> null
            }
        } catch (e: Exception) {
            AgentServerMessage.Error("خطا در پردازش پیام سرور: ${e.message}")
        }
    }
}
