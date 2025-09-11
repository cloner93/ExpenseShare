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
import org.milad.expense_share.model.FriendRequestDto
import org.milad.expense_share.model.FriendRequestsResponse

fun Routing.friendRoutes() {
    authenticate("auth-jwt") {
        route("/friends") {

            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()
                val friends = FakeDatabase.getFriends(userId)
                call.respond(friends)
            }

            get("/requests") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val incoming = FakeDatabase.getIncomingRequests(userId)
                val outgoing = FakeDatabase.getOutgoingRequests(userId)

                call.respond(FriendRequestsResponse(incoming, outgoing))
            }

            post("/request") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val req = call.receive<FriendRequestDto>()
                val success = FakeDatabase.sendFriendRequest(userId, req.phone)

                if (success) {
                    call.respond(HttpStatusCode.OK, "Friend request sent")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "User not found or already requested")
                }
            }

            post("/accept") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val req = call.receive<FriendRequestDto>()
                val success = FakeDatabase.acceptFriendRequest(userId, req.phone)

                if (success) {
                    call.respond(HttpStatusCode.OK, "Friend request accepted")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No pending request found")
                }
            }

            post("/reject") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val req = call.receive<FriendRequestDto>()
                val success = FakeDatabase.rejectFriendRequest(userId, req.phone)

                if (success) {
                    call.respond(HttpStatusCode.OK, "Friend request rejected")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No pending request found")
                }
            }

            delete("/{phone}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()
                val phone = call.parameters["phone"] ?: return@delete call.respond(
                    mapOf("success" to false, "message" to "Phone is required")
                )

                val success = FakeDatabase.removeFriend(userId, phone)

                if (success) {
                    call.respond(HttpStatusCode.OK, "Friend removed")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Friend not found")
                }
            }
        }
    }
}
