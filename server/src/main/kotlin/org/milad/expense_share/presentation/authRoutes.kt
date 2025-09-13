package org.milad.expense_share.presentation

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
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.model.LoginRequest
import org.milad.expense_share.model.RegisterRequest

internal fun Routing.authRoutes(authService: AuthService) {

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(
                username = request.username,
                phone = request.phone,
                password = request.password
            )
            call.respond(
                if (response.success)
                    HttpStatusCode.Created else HttpStatusCode.BadRequest,
                response
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request.phone, request.password)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized,
                response
            )
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