package org.milad.expense_share.presentation.chat

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import org.milad.expense_share.data.models.chat.ChatHistoryResponse
import org.milad.expense_share.data.models.chat.SendMessageRequest
import org.milad.expense_share.domain.service.ChatService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId
import java.util.concurrent.ConcurrentHashMap

private val groupConnections = ConcurrentHashMap<Int, MutableSet<WebSocketSession>>()

internal fun Routing.chatRoutes(chatService: ChatService) {

    val json = Json { prettyPrint = true }

    authenticate("auth-jwt") {

        webSocket("/chat/{groupId}") {
            val userId = call.principal<JWTPrincipal>().getUserId()
                ?: return@webSocket close(
                    CloseReason(
                        CloseReason.Codes.VIOLATED_POLICY,
                        "Invalid token"
                    )
                )

            val groupId = call.getIntParameter("groupId")
                ?: return@webSocket close(
                    CloseReason(
                        CloseReason.Codes.CANNOT_ACCEPT,
                        "Invalid group ID"
                    )
                )

            val connections = groupConnections.getOrPut(groupId) { mutableSetOf() }
            connections.add(this)

            try {
                val session = chatService.getOrCreateChatSession(groupId)
                val history = chatService.getChatHistory(session.id)

                send(
                    Frame.Text(
                        json.encodeToString(
                            SuccessResponse(
                                success = true,
                                data = ChatHistoryResponse(
                                    session = "HISTORY",
                                    messages = history
                                )
                            )
                        )
                    )
                )

                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()

                        try {
                            val request = json.decodeFromString<SendMessageRequest>(text)

                            chatService.handleUserMessage(userId, groupId, request.content)
                                .onSuccess { messages ->
                                    broadcastToGroup(
                                        groupId, json.encodeToString(
                                            SuccessResponse(
                                                success = true,
                                                data = ChatHistoryResponse(
                                                    session = "NEW_MESSAGE",
                                                    messages = messages
                                                )
                                            )
                                        )
                                    )
                                }
                                .onFailure { error ->
                                    send(
                                        Frame.Text(
                                            json.encodeToString(
                                                ErrorResponse(
                                                    message = error.message
                                                        ?: "Failed to send message",
                                                    code = "SEND_MESSAGE_FAILED"
                                                )
                                            )
                                        )
                                    )
                                }

                        } catch (e: Exception) {
                            send(
                                Frame.Text(
                                    json.encodeToString(
                                        ErrorResponse(
                                            message = "Invalid message format+ ${e.message}",
                                            code = "INVALID_FORMAT"
                                        )
                                    )
                                )
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connections.remove(this)
                if (connections.isEmpty()) {
                    groupConnections.remove(groupId)
                }
            }
        }

        get("/chat/history/{groupId}") {
            val userId = call.principal<JWTPrincipal>().getUserId()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Invalid token", "INVALID_TOKEN")
                )

            val groupId = call.getIntParameter("groupId")
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid group ID", "INVALID_GROUP_ID")
                )

            try {
                val session = chatService.getOrCreateChatSession(groupId)
                val messages = chatService.getChatHistory(session.id)

                call.respond(
                    HttpStatusCode.OK, SuccessResponse(
                        data = mapOf(
                            "sessionId" to session.id,
                            "messages" to messages
                        )
                    )
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(e.message ?: "Failed to fetch chat history", "FETCH_FAILED")
                )
            }
        }
    }
}

private suspend fun broadcastToGroup(groupId: Int, message: String) {
    groupConnections[groupId]?.forEach { session ->
        try {
            session.send(Frame.Text(message))
        } catch (e: Exception) {
        }
    }
}