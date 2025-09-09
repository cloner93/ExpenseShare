package org.milad.expense_share.routing

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.milad.expense_share.database.FakeDatabase
import org.milad.expense_share.model.CreateGroupRequest

internal fun Routing.groupsRoutes() {
    route("/groups") {
        authenticate("auth-jwt") {
            post("/create") {

                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()

                val req = call.receive<CreateGroupRequest>()
                val group = FakeDatabase.createGroup(userId, req.name)

                call.respond(group)
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()

                val groups = FakeDatabase.getGroupsOfUser(userId)
                call.respond(groups)
            }
            post("/create/{id}/addUser") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()

            }
        }
    }
}
