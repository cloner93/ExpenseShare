package org.milad.expense_share.routing

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
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
            val res = FakeDatabase.register(req.username, req.phone, req.password)
            call.respond(res)
        }
        post("/login") {
            val req = call.receive<LoginRequest>()
            logIt(req.toString())

            val res = FakeDatabase.login(req.phone, req.password)
            call.respond(res)
        }
    }
}
fun logIt(msg: String) = println("@@@@-> $msg")