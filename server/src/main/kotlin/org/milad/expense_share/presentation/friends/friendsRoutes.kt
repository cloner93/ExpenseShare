package org.milad.expense_share.presentation.friends

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.presentation.friends.model.FriendRequest
import org.milad.expense_share.utils.ErrorCodes
import org.milad.expense_share.utils.Messages
import org.milad.expense_share.utils.getUserId

internal fun Routing.friendRoutes(
    friendsService: FriendsService
) {
    authenticate("auth-jwt") {
        route("/friends") {

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                val statusParam = call.request.queryParameters["status"]
                val status = statusParam?.let {
                    try {
                        FriendRelationStatus.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                Messages.INVALID_STATUS,
                                ErrorCodes.INVALID_STATUS
                            )
                        )
                    }
                }

                friendsService.getAllFriends(userId, status)
                    .onSuccess { friends ->
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(data = friends)

                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: Messages.FETCH_FRIENDS_FAILED,
                                ErrorCodes.FETCH_FAILED
                            )
                        )
                    }
            }

            get("/accepted") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                friendsService.getAcceptedFriends(userId)
                    .onSuccess { friends ->
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(data = friends)
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: Messages.FETCH_FRIENDS_FAILED,
                                ErrorCodes.FETCH_FAILED
                            )
                        )
                    }
            }

            get("/requests/incoming") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                friendsService.getIncomingRequests(userId)
                    .onSuccess { requests ->
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(data = requests)
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: Messages.FETCH_REQUESTS_FAILED,
                                ErrorCodes.FETCH_FAILED
                            )
                        )
                    }
            }

            get("/requests/outgoing") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                friendsService.getOutgoingRequests(userId)
                    .onSuccess { requests ->
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(data = requests)
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: Messages.FETCH_REQUESTS_FAILED,
                                ErrorCodes.FETCH_FAILED
                            )
                        )
                    }
            }

            get("/blocked") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                friendsService.getBlockedFriends(userId)
                    .onSuccess { blocked ->
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(data = blocked)
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: Messages.FETCH_BLOCKED_FAILED,
                                ErrorCodes.FETCH_FAILED
                            )
                        )
                    }
            }

            get("/status/{phone}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                val phone = call.parameters["phone"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(Messages.PHONE_REQUIRED, ErrorCodes.PHONE_REQUIRED)
                    )

                friendsService.getFriendshipStatus(userId, phone)
                    .onSuccess { status ->
                        if (status != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                SuccessResponse(data = status)
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse(Messages.NO_FRIENDSHIP_FOUND, ErrorCodes.NOT_FOUND)
                            )
                        }
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(error.message ?: Messages.STATUS_FAILED, ErrorCodes.STATUS_FAILED)
                        )
                    }
            }

            post("/request") {
                call.handleFriendAction("send_request") { userId, phone ->
                    friendsService.sendFriendRequest(userId, phone)
                }
            }

            put("/accept") {
                call.handleFriendAction("accept") { userId, phone ->
                    friendsService.acceptFriendRequest(userId, phone)
                }
            }

            put("/reject") {
                call.handleFriendAction("reject") { userId, phone ->
                    friendsService.rejectFriendRequest(userId, phone)
                }
            }

            put("/block") {
                call.handleFriendAction("block") { userId, phone ->
                    friendsService.blockFriend(userId, phone)
                }
            }

            put("/unblock") {
                call.handleFriendAction("unblock") { userId, phone ->
                    friendsService.unblockFriend(userId, phone)
                }
            }

            put("/cancel") {
                call.handleFriendAction("cancel") { userId, phone ->
                    friendsService.cancelFriendRequest(userId, phone)
                }
            }

            delete("/remove") {
                call.handleFriendAction("remove") { userId, phone ->
                    friendsService.removeFriend(userId, phone)
                }
            }
        }
    }
}

private suspend fun ApplicationCall.handleFriendAction(
    actionName: String,
    block: suspend (Int, String) -> Result<String>,
) {
    val userId = principal<JWTPrincipal>().getUserId()
        ?: return respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
        )

    val request = receive<FriendRequest>()

    if (request.phone.isBlank()) {
        return respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(Messages.PHONE_REQUIRED, ErrorCodes.PHONE_REQUIRED)
        )
    }

    block(userId, request.phone)
        .onSuccess { message ->
            respond(HttpStatusCode.OK, SuccessResponse(data = message))
        }
        .onFailure { error ->
            val (statusCode, errorCode) = when {
                error.message?.contains("not found", ignoreCase = true) == true ->
                    HttpStatusCode.NotFound to ErrorCodes.NOT_FOUND

                error.message?.contains("already", ignoreCase = true) == true ->
                    HttpStatusCode.Conflict to ErrorCodes.ALREADY_EXISTS

                error.message?.contains("Cannot", ignoreCase = true) == true ->
                    HttpStatusCode.BadRequest to ErrorCodes.INVALID_ACTION

                else ->
                    HttpStatusCode.InternalServerError to "${actionName.uppercase()}_FAILED"
            }

            respond(
                statusCode,
                ErrorResponse(error.message ?: Messages.ACTION_FAILED, errorCode)
            )
        }
}
