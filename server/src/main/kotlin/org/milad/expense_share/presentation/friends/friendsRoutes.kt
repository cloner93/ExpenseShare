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
import org.milad.expense_share.presentation.friends.model.FriendsListResponse
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
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val statusParam = call.request.queryParameters["status"]
                val status = statusParam?.let {
                    try {
                        FriendRelationStatus.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                "Invalid status. Valid values: PENDING, ACCEPTED, BLOCKED, REJECTED",
                                "INVALID_STATUS"
                            )
                        )
                    }
                }

                friendsService.getAllFriends(userId, status)
                    .onSuccess { friends ->
                        call.respond(
                            HttpStatusCode.OK,
                            FriendsListResponse(
                                friends = friends,
                                total = friends.size
                            )
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: "Failed to fetch friends",
                                "FETCH_FAILED"
                            )
                        )
                    }
            }

            get("/accepted") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                friendsService.getAcceptedFriends(userId)
                    .onSuccess { friends ->
                        call.respond(HttpStatusCode.OK, FriendsListResponse(friends, friends.size))
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: "Failed to fetch friends",
                                "FETCH_FAILED"
                            )
                        )
                    }
            }

            get("/requests/incoming") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                friendsService.getIncomingRequests(userId)
                    .onSuccess { requests ->
                        call.respond(
                            HttpStatusCode.OK,
                            FriendsListResponse(requests, requests.size)
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: "Failed to fetch requests",
                                "FETCH_FAILED"
                            )
                        )
                    }
            }

            get("/requests/outgoing") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                friendsService.getOutgoingRequests(userId)
                    .onSuccess { requests ->
                        call.respond(
                            HttpStatusCode.OK,
                            FriendsListResponse(requests, requests.size)
                        )
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: "Failed to fetch requests",
                                "FETCH_FAILED"
                            )
                        )
                    }
            }

            get("/blocked") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                friendsService.getBlockedFriends(userId)
                    .onSuccess { blocked ->
                        call.respond(HttpStatusCode.OK, FriendsListResponse(blocked, blocked.size))
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                error.message ?: "Failed to fetch blocked users",
                                "FETCH_FAILED"
                            )
                        )
                    }
            }

            get("/status/{phone}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val phone = call.parameters["phone"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Phone number is required", "PHONE_REQUIRED")
                    )

                friendsService.getFriendshipStatus(userId, phone)
                    .onSuccess { status ->
                        if (status != null) {
                            call.respond(HttpStatusCode.OK, status)
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse("No friendship found", "NOT_FOUND")
                            )
                        }
                    }
                    .onFailure { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(error.message ?: "Failed to get status", "STATUS_FAILED")
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
            ErrorResponse("Invalid token", "INVALID_TOKEN")
        )

    val request = receive<FriendRequest>()

    if (request.phone.isBlank()) {
        return respond(
            HttpStatusCode.BadRequest,
            ErrorResponse("Phone number is required", "PHONE_REQUIRED")
        )
    }

    block(userId, request.phone)
        .onSuccess { message ->
            respond(HttpStatusCode.OK, SuccessResponse(data = message))
        }
        .onFailure { error ->
            val (statusCode, errorCode) = when {
                error.message?.contains("not found", ignoreCase = true) == true ->
                    HttpStatusCode.NotFound to "NOT_FOUND"

                error.message?.contains("already", ignoreCase = true) == true ->
                    HttpStatusCode.Conflict to "ALREADY_EXISTS"

                error.message?.contains("Cannot", ignoreCase = true) == true ->
                    HttpStatusCode.BadRequest to "INVALID_ACTION"

                else ->
                    HttpStatusCode.InternalServerError to "${actionName.uppercase()}_FAILED"
            }

            respond(
                statusCode,
                ErrorResponse(error.message ?: "Action failed", errorCode)
            )
        }
}