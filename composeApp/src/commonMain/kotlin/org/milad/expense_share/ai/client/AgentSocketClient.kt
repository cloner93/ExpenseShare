package org.milad.expense_share.ai.client

import client.HttpConfig
import getDefaultBaseUrl
import io.ktor.client.HttpClient
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _serverMessages = MutableSharedFlow<AgentServerMessage>(extraBufferCapacity = 64)
    val serverMessages: SharedFlow<AgentServerMessage> = _serverMessages.asSharedFlow()

    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null
    private var isRunning = false

    fun start() {
        if (isRunning && connectionJob?.isActive == true) return
        isRunning = true

        connectionJob = scope.launch {
            while (isActive && isRunning) {
                val token = tokenProvider.getToken()?.accessToken
                if (token.isNullOrBlank()) {
                    _isConnected.value = false
                    delay(2000)
                    continue
                }

                try {
                    val base = if (httpConfig.baseUrl.isNotBlank()) httpConfig.baseUrl else getDefaultBaseUrl()
                    val isHttps = URLBuilder(base).protocol.isSecure()

                    val wsSession = httpClient.webSocketSession {
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

                    session = wsSession
                    _isConnected.value = true
                    println("[AgentSocketClient] Connected to WebSocket at $base")

                    for (frame in wsSession.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("[AgentSocketClient] Received frame: $text")
                            val parsed = parseServerMessage(text)
                            println("[AgentSocketClient] Parsed server message: $parsed")
                            if (parsed != null) {
                                _serverMessages.emit(parsed)
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    println("[AgentSocketClient] WebSocket cancelled")
                    break
                } catch (e: Exception) {
                    println("[AgentSocketClient] WebSocket exception: ${e.message}")
                    _serverMessages.emit(AgentServerMessage.Error(e.message ?: "خطای ارتباط با سرور"))
                } finally {
                    println("[AgentSocketClient] WebSocket connection closed, cleaning session")
                    _isConnected.value = false
                    session = null
                }

                if (isActive && isRunning) {
                    delay(3000)
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        connectionJob?.cancel()
        connectionJob = null
        _isConnected.value = false
        val s = session
        session = null
        scope.launch {
            try {
                s?.close(CloseReason(CloseReason.Codes.NORMAL, "Client stopped"))
            } catch (_: Exception) {}
        }
    }

    suspend fun sendUserMessage(sessionId: String, text: String, imageBase64: String? = null) {
        val s = session
        println("[AgentSocketClient] sendUserMessage: session=$s, isActive=${s?.isActive}")
        if (s != null && s.isActive) {
            val payload: AgentClientMessage = AgentClientMessage.SendMessage(
                sessionId = sessionId,
                text = text,
                imageBase64 = imageBase64
            )
            val payloadText = json.encodeToString(AgentClientMessage.serializer(), payload)
            println("[AgentSocketClient] Sending payload: $payloadText")
            s.send(Frame.Text(payloadText))
            println("[AgentSocketClient] Frame sent successfully")
        } else {
            println("[AgentSocketClient] Socket not connected! Cannot send message.")
            throw IllegalStateException("اتصال سوکت برقرار نیست")
        }
    }

    suspend fun confirmDraft(
        sessionId: String,
        draftId: String,
        actionType: String,
        payload: JsonObject
    ) {
        val s = session
        if (s != null && s.isActive) {
            val message: AgentClientMessage = AgentClientMessage.ConfirmDraft(
                sessionId = sessionId,
                draftId = draftId,
                actionType = actionType,
                payload = payload
            )
            val payloadText = json.encodeToString(AgentClientMessage.serializer(), message)
            s.send(Frame.Text(payloadText))
        } else {
            throw IllegalStateException("اتصال سوکت برقرار نیست")
        }
    }

    suspend fun cancelDraft(sessionId: String, draftId: String) {
        val s = session
        if (s != null && s.isActive) {
            val message: AgentClientMessage = AgentClientMessage.CancelDraft(
                sessionId = sessionId,
                draftId = draftId
            )
            val payloadText = json.encodeToString(AgentClientMessage.serializer(), message)
            s.send(Frame.Text(payloadText))
        } else {
            throw IllegalStateException("اتصال سوکت برقرار نیست")
        }
    }

    private fun parseServerMessage(rawText: String): AgentServerMessage? {
        return try {
            val jsonElement = json.parseToJsonElement(rawText).jsonObject
            val type = jsonElement["type"]?.jsonPrimitive?.contentOrNull

            when {
                type == "Thinking" || jsonElement.containsKey("status") -> {
                    val status = jsonElement["status"]?.jsonPrimitive?.contentOrNull ?: "در حال پردازش..."
                    AgentServerMessage.Thinking(status)
                }
                type == "MessageChunk" || (jsonElement.containsKey("text") && !jsonElement.containsKey("cardType")) -> {
                    val text = jsonElement["text"]?.jsonPrimitive?.contentOrNull ?: ""
                    val isFinal = jsonElement["isFinal"]?.jsonPrimitive?.booleanOrNull ?: false
                    AgentServerMessage.MessageChunk(text, isFinal)
                }
                type == "GenerativeCard" || jsonElement.containsKey("cardType") -> {
                    val cardType = jsonElement["cardType"]?.jsonPrimitive?.contentOrNull ?: "ACTION_DRAFT"
                    val draftId = jsonElement["draftId"]?.jsonPrimitive?.contentOrNull
                    val data = jsonElement["data"]?.jsonObject ?: JsonObject(emptyMap())
                    AgentServerMessage.GenerativeCard(cardType, draftId, data)
                }
                type == "ActionExecuted" || (jsonElement.containsKey("success") && jsonElement.containsKey("draftId")) -> {
                    val draftId = jsonElement["draftId"]?.jsonPrimitive?.contentOrNull ?: ""
                    val success = jsonElement["success"]?.jsonPrimitive?.booleanOrNull ?: true
                    val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull ?: ""
                    AgentServerMessage.ActionExecuted(draftId, success, message)
                }
                type == "Error" || jsonElement.containsKey("message") -> {
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
