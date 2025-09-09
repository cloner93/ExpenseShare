package org.milad.expense_share.routing

import com.sun.tools.jdeprscan.Main.call
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

            // لیست دوستان تایید شده
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()
                val friends = FakeDatabase.getFriends(userId)
                call.respond(friends)
            }

            // لیست درخواست‌های دوستی (pending)
            get("/requests") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val incoming = FakeDatabase.getIncomingRequests(userId)
                val outgoing = FakeDatabase.getOutgoingRequests(userId)

                call.respond(FriendRequestsResponse(incoming, outgoing))
            }

            // ارسال درخواست دوستی
            post("/request") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val req = call.receive<FriendRequestDto>()
                val success = FakeDatabase.sendFriendRequest(userId, req.phone)

                if (success) {
                    call.respond(mapOf("success" to true, "message" to "Request sent"))
                } else {
                    call.respond(mapOf("success" to false, "message" to "User not found or already requested"))
                }
            }

            // قبول کردن درخواست دوستی
            post("/accept") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val req = call.receive<FriendRequestDto>()
                val success = FakeDatabase.acceptFriendRequest(userId, req.phone)

                if (success) {
                    call.respond(mapOf("success" to true, "message" to "Friend request accepted"))
                } else {
                    call.respond(mapOf("success" to false, "message" to "No pending request found"))
                }
            }

            // رد کردن درخواست دوستی
            post("/reject") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val req = call.receive<FriendRequestDto>()
                val success = FakeDatabase.rejectFriendRequest(userId, req.phone)

                if (success) {
                    call.respond(mapOf("success" to true, "message" to "Friend request rejected"))
                } else {
                    call.respond(mapOf("success" to false, "message" to "No pending request found"))
                }
            }

            // حذف دوست
            delete("/{phone}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()
                val phone = call.parameters["phone"] ?: return@delete call.respond(
                    mapOf("success" to false, "message" to "Phone is required")
                )

                val success = FakeDatabase.removeFriend(userId, phone)

                if (success) {
                    call.respond(mapOf("success" to true, "message" to "Friend removed"))
                } else {
                    call.respond(mapOf("success" to false, "message" to "Friend not found"))
                }
            }
        }
    }
}
