package org.milad.expense_share.routing

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
import org.milad.expense_share.database.FakeDatabase
import org.milad.expense_share.model.LoginRequest
import org.milad.expense_share.model.RegisterRequest

internal fun Routing.authRoutes() {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            logIt(req.toString())

            val registerRes = FakeDatabase.register(req.username, req.phone, req.password)

            if (registerRes.success) {
                registerRes.user?.let {
                    call.respond(registerRes)
                }
            } else {
                call.respond(registerRes)
            }
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            logIt(req.toString())

            val loginRes = FakeDatabase.login(req.phone, req.password)

            if (loginRes.success) {
                loginRes.user?.let {
                    call.respond(loginRes)
                }
            } else {
                call.respond(loginRes)
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

fun logIt(msg: String) = println("LOG -> $msg")