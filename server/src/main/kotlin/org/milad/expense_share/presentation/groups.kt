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
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService
import org.milad.expense_share.model.AddUserRequest
import org.milad.expense_share.model.CreateGroupRequest
import org.milad.expense_share.model.CreateTransactionRequest
import org.milad.expense_share.model.ErrorResponse
import org.milad.expense_share.model.SuccessResponse
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId

internal fun Routing.groupsRoutes(
    groupService: GroupService,
    transactionService: TransactionService
) {
    authenticate("auth-jwt") {
        route("/groups") {

            post("/create") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val request = call.receive<CreateGroupRequest>()
                groupService.createGroup(userId, request.name, request.memberIds)
                    .onSuccess { call.respond(HttpStatusCode.Created, it) }
                    .onFailure { call.respond(HttpStatusCode.BadRequest, ErrorResponse(it.message ?: "Failed")) }
            }

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                call.respond(HttpStatusCode.OK, groupService.getUserGroups(userId))
            }

            post("/{groupId}/updateMembers") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val groupId = call.getIntParameter("groupId")
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                val request = call.receive<AddUserRequest>()
                groupService.addUsers(userId, groupId, request.memberIds)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(it)) }
                    .onFailure { call.respond(HttpStatusCode.Forbidden, ErrorResponse(it.message ?: "Failed")) }
            }

            delete("/{groupId}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                val groupId = call.getIntParameter("groupId")
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                groupService.deleteGroup(userId, groupId)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(it)) }
                    .onFailure { call.respond(HttpStatusCode.Forbidden, ErrorResponse(it.message ?: "Failed")) }
            }

            route("/{groupId}/transactions") {

                post {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                    val groupId = call.getIntParameter("groupId")
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                    val request = call.receive<CreateTransactionRequest>()
                    transactionService.createTransaction(groupId, userId, request.title, request.amount, request.description)
                        .onSuccess { call.respond(HttpStatusCode.Created, it) }
                        .onFailure { call.respond(HttpStatusCode.NotFound, ErrorResponse(it.message ?: "Failed")) }
                }

                get {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

                    val groupId = call.getIntParameter("groupId")
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID"))

                    call.respond(HttpStatusCode.OK, transactionService.getTransactions(userId, groupId))
                }

                post("/{transactionId}/approve") {
                    call.handleTransactionAction("approve") { id, uid ->
                        transactionService.approve(id, uid)
                    }
                }

                post("/{transactionId}/reject") {
                    call.handleTransactionAction("reject") { id, uid ->
                        transactionService.reject(id, uid)
                    }
                }

                delete("/{transactionId}") {
                    call.handleTransactionAction("delete") { id, uid ->
                        transactionService.delete(id, uid)
                    }
                }
            }
        }
    }
}
private suspend fun ApplicationCall.handleTransactionAction(
    actionName: String,
    block: (Int, Int) -> Result<String>
) {
    val userId = principal<JWTPrincipal>().getUserId()
        ?: return respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))

    val transactionId = getIntParameter("transactionId")
        ?: return respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid transaction ID"))

    block(transactionId, userId)
        .onSuccess { respond(HttpStatusCode.OK, SuccessResponse(it)) }
        .onFailure { respond(HttpStatusCode.Forbidden, ErrorResponse(it.message ?: "Failed to $actionName")) }
}