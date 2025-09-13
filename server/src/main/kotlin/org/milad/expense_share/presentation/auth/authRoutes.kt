package org.milad.expense_share.presentation.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.presentation.auth.model.LoginRequest
import org.milad.expense_share.presentation.auth.model.RegisterRequest
import org.milad.expense_share.presentation.api_model.ErrorResponse
import org.milad.expense_share.presentation.api_model.SuccessResponse

internal fun Routing.authRoutes(authService: AuthService) {

    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            authService.register(
                username = request.username,
                phone = request.phone,
                password = request.password
            ).onSuccess {
                call.respond(HttpStatusCode.Created, SuccessResponse(data = it))
            }.onFailure {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(it.message ?: "Registration failed", "REGISTER_FAILED")
                )
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            authService.login(request.phone, request.password)
                .onSuccess {
                    call.respond(HttpStatusCode.OK, SuccessResponse(data = it))
                }
                .onFailure {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(it.message ?: "Invalid credentials", "INVALID_CREDENTIALS")
                    )
                }
        }
    }
}