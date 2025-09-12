package org.milad.expense_share.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.milad.expense_share.database.InMemoryUserRepository
import org.milad.expense_share.database.UserRepository
import org.milad.expense_share.model.ErrorResponse
import org.milad.expense_share.model.LoginRequest
import org.milad.expense_share.model.RegisterRequest
import org.milad.expense_share.utils.validate

internal fun Routing.authRoutes() {
    val userRepository : UserRepository= InMemoryUserRepository()

    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                // Validate input
                val validationError = request.validate()
                if (validationError != null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(validationError, "VALIDATION_ERROR")
                    )
                }

                val registerResult = userRepository.register(
                    username = request.username.trim(),
                    phone = request.phone.trim(),
                    password = request.password
                )

                if (registerResult.success) {
                    call.respond(HttpStatusCode.Created, registerResult)
                } else {

                    val statusCode = when {
                        registerResult.message.contains("Phone already registered", true) -> HttpStatusCode.Conflict
                        else -> HttpStatusCode.BadRequest
                    }
                    call.respond(statusCode, registerResult)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid request format: ${e.message}", "INVALID_REQUEST")
                )
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                val validationError = request.validate()
                if (validationError != null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(validationError, "VALIDATION_ERROR")
                    )
                }

                val loginResult = userRepository.login(
                    phone = request.phone.trim(),
                    password = request.password
                )

                if (loginResult.success) {
                    call.respond(HttpStatusCode.OK, loginResult)
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid phone number or password", "INVALID_CREDENTIALS")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid request format: ${e.message}", "INVALID_REQUEST")
                )
            }
        }
    }

    authenticate("auth-jwt") {
        get("/test") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("username").asString()
            val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
            call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
        }
    }
}