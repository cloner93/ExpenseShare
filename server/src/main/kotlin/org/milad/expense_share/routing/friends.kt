package org.milad.expense_share.routing

import io.ktor.http.HttpStatusCode
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
import org.milad.expense_share.database.FakeDatabase
import org.milad.expense_share.model.ErrorResponse
import org.milad.expense_share.model.FriendRequestDto
import org.milad.expense_share.model.FriendRequestsResponse
import org.milad.expense_share.model.SuccessResponse
import org.milad.expense_share.utils.getStringParameter
import org.milad.expense_share.utils.getUserId

internal fun Routing.friendRoutes() {
    authenticate("auth-jwt") {
        route("/friends") {

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token")
                    )

                try {
                    val friends = FakeDatabase.getFriends(userId)
                    call.respond(HttpStatusCode.OK, friends)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to fetch friends: ${e.message}"))
                }
            }

            get("/requests") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                try {
                    val incoming = FakeDatabase.getIncomingRequests(userId)
                    val outgoing = FakeDatabase.getOutgoingRequests(userId)

                    call.respond(HttpStatusCode.OK, FriendRequestsResponse(incoming, outgoing))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to fetch friend requests: ${e.message}"))
                }
            }

            post("/request") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                try {
                    val request = call.receive<FriendRequestDto>()

                    // Validate phone number
                    if (request.phone.isBlank()) {
                        return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Phone number is required"))
                    }

                    val success = FakeDatabase.sendFriendRequest(userId, request.phone)

                    if (success) {
                        call.respond(HttpStatusCode.OK,
                            SuccessResponse("Friend request sent successfully")
                        )
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("User not found or request already exists"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
                }
            }

            post("/accept") {
                handleFriendRequestAction(
                    actionName = "accept",
                    successMessage = "Friend request accepted successfully",
                    action = FakeDatabase::acceptFriendRequest
                )
            }

            post("/reject") {
                handleFriendRequestAction(
                    actionName = "reject",
                    successMessage = "Friend request rejected successfully",
                    action = FakeDatabase::rejectFriendRequest
                )
            }

            delete("/{phone}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val phone = call.getStringParameter("phone")
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Phone number is required"))

                try {
                    val success = FakeDatabase.removeFriend(userId, phone)

                    if (success) {
                        call.respond(HttpStatusCode.OK, SuccessResponse("Friend removed successfully"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Friend not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to remove friend: ${e.message}"))
                }
            }
        }
    }
}

private suspend fun io.ktor.server.routing.RoutingContext.handleFriendRequestAction(
    actionName: String,
    successMessage: String,
    action: (Int, String) -> Boolean
) {
    val userId = call.principal<JWTPrincipal>().getUserId()
        ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

    try {
        val request = call.receive<FriendRequestDto>()

        if (request.phone.isBlank()) {
            return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Phone number is required"))
        }

        val success = action(userId, request.phone)

        if (success) {
            call.respond(HttpStatusCode.OK, SuccessResponse(successMessage))
        } else {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("No pending friend request found"))
        }
    } catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
    }
}