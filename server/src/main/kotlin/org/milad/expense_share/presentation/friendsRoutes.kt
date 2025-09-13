package org.milad.expense_share.presentation

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
import org.milad.expense_share.domain.service.FriendService
import org.milad.expense_share.model.ErrorResponse
import org.milad.expense_share.model.FriendRequestDto
import org.milad.expense_share.model.FriendRequestsResponse
import org.milad.expense_share.model.SuccessResponse
import org.milad.expense_share.utils.getStringParameter
import org.milad.expense_share.utils.getUserId


internal fun Routing.friendRoutes(friendService: FriendService) {
    authenticate("auth-jwt") {
        route("/friends") {

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                call.respond(HttpStatusCode.OK, friendService.listFriends(userId))
            }

            get("/requests") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val (incoming, outgoing) = friendService.listRequests(userId)
                call.respond(HttpStatusCode.OK, FriendRequestsResponse(incoming, outgoing))
            }

            post("/request") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val request = call.receive<FriendRequestDto>()
                if (request.phone.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Phone number is required"))
                }

                friendService.sendRequest(userId, request.phone)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(it)) }
                    .onFailure { call.respond(HttpStatusCode.BadRequest, ErrorResponse(it.message ?: "Failed")) }
            }

            post("/accept") {
                call.handleFriendAction { uid, phone -> friendService.acceptRequest(uid, phone) }
            }

            post("/reject") {
                call.handleFriendAction { uid, phone -> friendService.rejectRequest(uid, phone) }
            }

            delete("/{phone}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val phone = call.getStringParameter("phone")
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Phone number is required"))

                friendService.removeFriend(userId, phone)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(it)) }
                    .onFailure { call.respond(HttpStatusCode.NotFound, ErrorResponse(it.message ?: "Failed")) }
            }
        }
    }
}

private suspend fun ApplicationCall.handleFriendAction(
    block: (Int, String) -> Result<String>
) {
    val userId = principal<JWTPrincipal>().getUserId()
        ?: return respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

    val request = receive<FriendRequestDto>()
    if (request.phone.isBlank()) {
        return respond(HttpStatusCode.BadRequest, ErrorResponse("Phone number is required"))
    }

    block(userId, request.phone)
        .onSuccess { respond(HttpStatusCode.OK, SuccessResponse(it)) }
        .onFailure { respond(HttpStatusCode.NotFound, ErrorResponse(it.message ?: "Failed")) }
}