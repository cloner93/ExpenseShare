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
import org.milad.expense_share.model.ErrorResponse
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId

internal fun Routing.groupsRoutes() {
    route("/groups") {
        authenticate("auth-jwt") {

            post("/create") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                try {
                    val request = call.receive<CreateGroupRequest>()
                    val group = FakeDatabase.createGroup(userId, request.name, request.memberIds)
                    call.respond(HttpStatusCode.Created, group)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
                }
            }

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                try {
                    val groups = FakeDatabase.getGroupsOfUser(userId)
                    call.respond(HttpStatusCode.OK, groups)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to fetch groups: ${e.message}"))
                }
            }

            post("/{groupId}/updateMembers") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token")
                    )

                val groupId = call.getIntParameter("groupId")
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                try {
                    val request = call.receive<AddUserRequest>()
                    val success = FakeDatabase.addUserToGroup(userId, groupId, request.memberIds)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Users added successfully"))
                    } else {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only group owner can add members"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
                }
            }

            delete("/{groupId}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val groupId = call.getIntParameter("groupId")
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                try {
                    val success = FakeDatabase.removeGroup(userId, groupId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Group deleted successfully"))
                    } else {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only group owner can delete the group"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to delete group: ${e.message}"))
                }
            }

            route("/{groupId}/transactions") {

                post {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                    val groupId = call.getIntParameter("groupId")
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                    try {
                        val request = call.receive<CreateTransactionRequest>()
                        val transaction = FakeDatabase.createTransaction(
                            groupId = groupId,
                            userId = userId,
                            title = request.title,
                            amount = request.amount,
                            description = request.description
                        )

                        if (transaction != null) {
                            call.respond(HttpStatusCode.Created, transaction)
                        } else {
                            call.respond(HttpStatusCode.NotFound, ErrorResponse("Group not found or access denied"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
                    }
                }

                get {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                    val groupId = call.getIntParameter("groupId")
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                    try {
                        val transactions = FakeDatabase.getTransactions(userId, groupId)
                        call.respond(HttpStatusCode.OK, transactions)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to fetch transactions: ${e.message}"))
                    }
                }

                post("/{transactionId}/approve") {
                    handleTransactionAction(
                        actionName = "approve",
                        action = FakeDatabase::approveTransaction
                    )
                }

                post("/{transactionId}/reject") {
                    handleTransactionAction(
                        actionName = "reject",
                        action = FakeDatabase::rejectTransaction
                    )
                }

                delete("/{transactionId}") {
                    handleTransactionAction(
                        actionName = "delete",
                        action = FakeDatabase::deleteTransaction
                    )
                }
            }
        }
    }
}

private suspend fun io.ktor.server.routing.RoutingContext.handleTransactionAction(
    actionName: String,
    action: (Int, Int) -> Boolean
) {
    val userId = call.principal<JWTPrincipal>().getUserId()
        ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

    val transactionId = call.getIntParameter("transactionId")
        ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid transaction ID"))

    try {
        val success = action(transactionId, userId)
        if (success) {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Transaction ${actionName}d successfully"))
        } else {
            call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only group owner can $actionName transactions"))
        }
    } catch (e: Exception) {
        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to $actionName transaction: ${e.message}"))
    }
}