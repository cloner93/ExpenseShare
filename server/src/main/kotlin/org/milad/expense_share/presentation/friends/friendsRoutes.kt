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
import io.ktor.server.routing.route
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.presentation.friends.model.FriendRequest
import org.milad.expense_share.presentation.friends.model.RequestsResponse
import org.milad.expense_share.utils.getStringParameter
import org.milad.expense_share.utils.getUserId


internal fun Routing.friendRoutes(friendsService: FriendsService) {
    authenticate("auth-jwt") {
        route("/friends") {

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                call.respond(
                    HttpStatusCode.OK,
                    SuccessResponse(data = friendsService.listFriends(userId))
                )
            }

            get("/requests") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val (incoming, outgoing) = friendsService.listRequests(userId)
                call.respond(
                    HttpStatusCode.OK,
                    SuccessResponse(data = RequestsResponse(incoming, outgoing))
                )
            }

            post("/request") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val request = call.receive<FriendRequest>()
                if (request.phone.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Phone number is required", "PHONE_REQUIRED")
                    )
                }

                friendsService.sendRequest(userId, request.phone)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                it.message ?: "Failed to send request",
                                "SEND_REQUEST_FAILED"
                            )
                        )
                    }
            }

            post("/accept") {
                call.handleFriendAction("accept") { uid, phone ->
                    friendsService.acceptRequest(uid, phone)
                }
            }

            post("/reject") {
                call.handleFriendAction("reject") { uid, phone ->
                    friendsService.rejectRequest(uid, phone)
                }
            }

            delete("/{phone}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val phone = call.getStringParameter("phone")
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Phone number is required", "PHONE_REQUIRED")
                    )

                friendsService.removeFriend(userId, phone)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(it.message ?: "Friend not found", "FRIEND_NOT_FOUND")
                        )
                    }
            }
        }
    }
}

private suspend fun ApplicationCall.handleFriendAction(
    actionName: String,
    block: (Int, String) -> Result<String>,
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
        .onSuccess { respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
        .onFailure {
            respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    it.message ?: "No pending friend request found",
                    "${actionName.uppercase()}_FAILED"
                )
            )
        }
}
