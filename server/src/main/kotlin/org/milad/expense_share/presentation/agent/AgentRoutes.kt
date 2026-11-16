package org.milad.expense_share.presentation.agent

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.milad.expense_share.ai.agent.AgentClientEvent
import org.milad.expense_share.ai.agent.AgentOrchestrator
import org.milad.expense_share.ai.agent.AgentServerEvent
import org.milad.expense_share.ai.mcp.model.AgentSecurityContext
import org.milad.expense_share.domain.repository.ConversationRepository
import org.milad.expense_share.utils.getUserId

@Serializable
data class CreateSessionRequest(val title: String = "گفتگوی جدید")

fun Route.agentRoutes(
    orchestrator: AgentOrchestrator,
    conversationRepository: ConversationRepository
) {
    val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    authenticate("auth-jwt") {
        route("/api/agent") {

            get("/sessions") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val sessions = conversationRepository.getUserSessions(userId)
                call.respond(HttpStatusCode.OK, sessions)
            }

            post("/sessions") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = try { call.receive<CreateSessionRequest>() } catch (e: Exception) { CreateSessionRequest() }
                val newSession = conversationRepository.createSession(userId, request.title)
                call.respond(HttpStatusCode.Created, newSession)
            }

            get("/sessions/{sessionId}/messages") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val sessionId = call.parameters["sessionId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing sessionId")

                val session = conversationRepository.getSession(sessionId, userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Session not found")

                val messages = conversationRepository.getMessages(session.id)
                call.respond(HttpStatusCode.OK, messages)
            }

            webSocket("/chat") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))

                println("[AgentRoutes] WebSocket client connected. userId=$userId")
                val context = AgentSecurityContext(userId = userId)

                suspend fun emitEvent(event: AgentServerEvent) {
                    val messageJson = json.encodeToString(AgentServerEvent.serializer(), event)
                    println("[AgentRoutes] Emitting event to client: $messageJson")
                    send(Frame.Text(messageJson))
                }

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        println("[AgentRoutes] Received text frame from client: $text")
                        try {
                            val clientEvent = json.decodeFromString<AgentClientEvent>(text)
                            println("[AgentRoutes] Decoded clientEvent: $clientEvent")
                            when (clientEvent) {
                                is AgentClientEvent.SendMessage -> {
                                    orchestrator.handleUserMessage(
                                        sessionId = clientEvent.sessionId,
                                        userText = clientEvent.text,
                                        context = context,
                                        onEvent = { emitEvent(it) }
                                    )
                                }
                                is AgentClientEvent.ConfirmDraft -> {
                                    orchestrator.handleDraftConfirmation(
                                        sessionId = clientEvent.sessionId,
                                        draftId = clientEvent.draftId,
                                        actionType = clientEvent.actionType,
                                        payload = clientEvent.payload,
                                        context = context,
                                        onEvent = { emitEvent(it) }
                                    )
                                }
                                is AgentClientEvent.CancelDraft -> {
                                    emitEvent(AgentServerEvent.ActionExecuted(
                                        draftId = clientEvent.draftId,
                                        success = false,
                                        message = "اقدام لغو شد."
                                    ))
                                }
                            }
                        } catch (e: Exception) {
                            emitEvent(AgentServerEvent.Error("Invalid event payload: ${e.message}"))
                        }
                    }
                }
            }
        }
    }
}
