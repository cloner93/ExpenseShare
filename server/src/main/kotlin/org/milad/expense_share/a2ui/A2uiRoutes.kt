package org.milad.expense_share.a2ui

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.milad.expense_share.a2ui.model.AgentInteractionRequest

fun Route.a2uiRoutes(agent: ExpenseShareA2uiAgent) {
    route("/api/a2ui/agent") {
        get("/scenarios") {
            val scenarios = agent.getAvailableScenarios()
            call.respond(HttpStatusCode.OK, scenarios)
        }

        post("/message") {
            try {
                val request = call.receive<AgentInteractionRequest>()
                val responseEnvelopes = agent.handleInteraction(request)
                call.respond(HttpStatusCode.OK, responseEnvelopes)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Failed to process A2UI agent message")))
            }
        }
    }
}
