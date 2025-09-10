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
import org.milad.expense_share.model.AddUserRequest
import org.milad.expense_share.model.CreateGroupRequest
import org.milad.expense_share.model.CreateTransactionRequest

internal fun Routing.groupsRoutes() {
    route("/groups") {
        authenticate("auth-jwt") {
            post("/create") {

                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()

                val req = call.receive<CreateGroupRequest>()

                val group = FakeDatabase.createGroup(userId, req.name, req.memberIds)

                call.respond(group)
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()

                val groups = FakeDatabase.getGroupsOfUser(userId)
                call.respond(groups)
            }

            post("/{groupId}/updateMembers") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()

                val groupId = call.parameters["groupId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid groupId")

                val req = call.receive<AddUserRequest>()
                FakeDatabase.addUserToGroup(userId, groupId, req.memberIds)

                call.respond(HttpStatusCode.OK, "User added successfully")
            }

            delete("/{groupId}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val groupId = call.parameters["groupId"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid groupId")

                val success = FakeDatabase.removeGroup(userId, groupId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Group deleted successfully")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Only the owner can delete the group")
                }
            }

            route("/{groupId}/transactions") {
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("id").asInt()
                    val groupId = call.parameters["groupId"]?.toIntOrNull()
                    val req = call.receive<CreateTransactionRequest>()
                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid groupId")
                    } else {
                        val tx = FakeDatabase.createTransaction(
                            groupId,
                            userId,
                            req.title,
                            req.amount,
                            req.description
                        )
                        if (tx != null) call.respond(tx) else call.respond(
                            HttpStatusCode.BadRequest,
                            "Group not found"
                        )
                    }
                }

                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("id").asInt()
                    val groupId = call.parameters["groupId"]?.toIntOrNull()

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid groupId")
                    } else {
                        val txs = FakeDatabase.getTransactions(userId,groupId)
                        call.respond(txs)
                    }
                }

                post("/{transactionId}/approve") {
                    val principal = call.principal<JWTPrincipal>()
                    val managerId = principal!!.payload.getClaim("id").asInt()
                    val transactionId = call.parameters["transactionId"]?.toIntOrNull()
                    if (transactionId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid transactionId")
                    } else {
                        val success = FakeDatabase.approveTransaction(transactionId, managerId)
                        if (success) call.respond(
                            HttpStatusCode.OK,
                            "Transaction approved"
                        ) else call.respond(HttpStatusCode.Forbidden, "Only owner can approve")
                    }
                }

                post("/{transactionId}/reject") {
                    val principal = call.principal<JWTPrincipal>()
                    val managerId = principal!!.payload.getClaim("id").asInt()
                    val transactionId = call.parameters["transactionId"]?.toIntOrNull()
                    if (transactionId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid transactionId")
                    } else {
                        val success = FakeDatabase.rejectTransaction(transactionId, managerId)
                        if (success) call.respond(
                            HttpStatusCode.OK,
                            "Transaction rejected"
                        ) else call.respond(HttpStatusCode.Forbidden, "Only owner can reject")
                    }
                }

                delete("/{transactionId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val managerId = principal!!.payload.getClaim("id").asInt()
                    val transactionId = call.parameters["transactionId"]?.toIntOrNull()
                    if (transactionId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid transactionId")
                    } else {
                        val success = FakeDatabase.deleteTransaction(transactionId, managerId)
                        if (success) call.respond(
                            HttpStatusCode.OK,
                            "Transaction deleted"
                        ) else call.respond(HttpStatusCode.Forbidden, "Only owner can delete")
                    }
                }
            }
        }
    }
}