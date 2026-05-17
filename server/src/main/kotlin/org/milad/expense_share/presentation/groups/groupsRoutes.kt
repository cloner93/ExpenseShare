package org.milad.expense_share.presentation.groups

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
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse
import org.milad.expense_share.presentation.groups.model.CreateGroupRequest
import org.milad.expense_share.utils.ErrorCodes
import org.milad.expense_share.utils.Messages
import org.milad.expense_share.utils.getIntParameter
import org.milad.expense_share.utils.getUserId

internal fun Routing.groupsRoutes(
    groupService: GroupService,
) {
    authenticate("auth-jwt") {
        route("/groups") {

            post("/create") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                val request = call.receive<CreateGroupRequest>()
                groupService.createGroup(userId, request.name, request.memberIds)
                    .onSuccess { call.respond(HttpStatusCode.Created, SuccessResponse(data = it)) }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                it.message ?: Messages.CREATE_GROUP_FAILED,
                                ErrorCodes.CREATE_GROUP_FAILED
                            )
                        )
                    }
            }

            get {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                groupService.getUserGroups(userId).onSuccess {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse(data = it)
                    )
                }.onFailure {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse(
                            it.message ?: Messages.NOT_GROUP_OWNER,
                            ErrorCodes.NOT_GROUP_OWNER
                        )
                    )
                }

            }

            post("/{groupId}/updateMembers") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                val groupId = call.getIntParameter("groupId")
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(Messages.INVALID_GROUP_ID, ErrorCodes.INVALID_GROUP_ID)
                    )

                val request = call.receive<List<Int>>()
                groupService.updateGroupUsers(userId, groupId, request)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(
                                it.message ?: Messages.FETCH_DATA_FAILED,
                                ErrorCodes.FETCH_DATA_FAILED
                            )
                        )
                    }
            }

            delete("/{groupId}") {
                val userId = call.principal<JWTPrincipal>().getUserId()
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(Messages.INVALID_TOKEN, ErrorCodes.INVALID_TOKEN)
                    )

                val groupId = call.getIntParameter("groupId")
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(Messages.INVALID_GROUP_ID, ErrorCodes.INVALID_GROUP_ID)
                    )

                groupService.deleteGroup(userId, groupId)
                    .onSuccess { call.respond(HttpStatusCode.OK, SuccessResponse(data = it)) }
                    .onFailure {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(
                                it.message ?: Messages.NOT_GROUP_OWNER,
                                ErrorCodes.NOT_GROUP_OWNER
                            )
                        )
                    }
            }
        }
    }
}
