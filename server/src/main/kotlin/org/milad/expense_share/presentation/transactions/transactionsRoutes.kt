package org.milad.expense_share.presentation.transactions

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
import org.milad.expense_share.domain.service.TransactionService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.presentation.transactions.model.CreateTransactionRequest
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId


internal fun Routing.transactionsRoutes(
    transactionService: TransactionService,
) {
    authenticate("auth-jwt") {
        route("/groups/{groupId}/transactions") {
            post {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val groupId = call.getIntParameter("groupId")
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid group ID", "INVALID_GROUP_ID")
                    )

                val request = call.receive<CreateTransactionRequest>()
                transactionService.createTransaction(
                    groupId,
                    userId,
                    request.title,
                    request.amount,
                    request.description,
                    request.payers,
                    request.shareDetails
                )
                    .onSuccess {
                        call.respond(
                            HttpStatusCode.Created,
                            SuccessResponse(data = it)
                        )
                    }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(
                                it.message ?: "Group not found or access denied",
                                "GROUP_NOT_FOUND"
                            )
                        )
                    }
            }

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid token", "INVALID_TOKEN")
                    )

                val groupId = call.getIntParameter("groupId")
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid group ID", "INVALID_GROUP_ID")
                    )

                call.respond(
                    HttpStatusCode.OK,
                    SuccessResponse(data = transactionService.getTransactions(userId, groupId))
                )
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

private suspend fun ApplicationCall.handleTransactionAction(
    actionName: String,
    block: (Int, Int) -> Result<String>,
) {
    val userId = principal<JWTPrincipal>().getUserId()
        ?: return respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse("Invalid token", "INVALID_TOKEN")
        )

    val transactionId = getIntParameter("transactionId")
        ?: return respond(
            HttpStatusCode.BadRequest,
            ErrorResponse("Invalid transaction ID", "INVALID_TRANSACTION_ID")
        )

    block(transactionId, userId)
        .onSuccess { respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
        .onFailure {
            respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    it.message ?: "Failed to $actionName",
                    "${actionName.uppercase()}_FAILED"
                )
            )
        }
}