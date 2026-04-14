package org.milad.expense_share.presentation.settlement

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.milad.expense_share.domain.service.SettlementService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId

internal fun Routing.settlementRoutes(
    settlementService: SettlementService
) {
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

                settlementService.groupSettlement(groupId = groupId, userId = userId).onSuccess {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse(data = it)
                    )
                }.onFailure {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse(
                            it.message ?: "Only group users can see settlement.",
                            "NOT_GROUP_OWNER"
                        )
                    )
                }

            }
        }
    }
}