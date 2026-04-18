package org.milad.expense_share.presentation.settlement

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.milad.expense_share.domain.service.SettlementService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId

internal fun Routing.settlementRoutes(settlementService: SettlementService) {

    authenticate("auth-jwt") {
        route("/groups/{groupId}/settlement") {

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

                settlementService.getGroupSettlements(groupId, userId)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(it.message ?: "Access denied", "ACCESS_DENIED")
                        )
                    }
            }

            route("/{settlementId}") {
                post("/pay") {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("Invalid token", "INVALID_TOKEN")
                        )

                    val settlementId = call.getIntParameter("settlementId")
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid settlement ID", "INVALID_SETTLEMENT_ID")
                        )

                    settlementService.markAsPaid(settlementId, userId)
                        .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                        .onFailure {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ErrorResponse(it.message ?: "Action not allowed", "ACTION_NOT_ALLOWED")
                            )
                        }
                }
                post("/confirm") {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("Invalid token", "INVALID_TOKEN")
                        )

                    val settlementId = call.getIntParameter("settlementId")
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid settlement ID", "INVALID_SETTLEMENT_ID")
                        )

                    settlementService.confirmPayment(settlementId, userId)
                        .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                        .onFailure {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ErrorResponse(it.message ?: "Action not allowed", "ACTION_NOT_ALLOWED")
                            )
                        }
                }
                post("/dispute") {
                    val userId = call.principal<JWTPrincipal>().getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("Invalid token", "INVALID_TOKEN")
                        )

                    val settlementId = call.getIntParameter("settlementId")
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid settlement ID", "INVALID_SETTLEMENT_ID")
                        )

                    settlementService.disputePayment(settlementId, userId)
                        .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                        .onFailure {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ErrorResponse(it.message ?: "Action not allowed", "ACTION_NOT_ALLOWED")
                            )
                        }
                }
            }
        }
    }
}