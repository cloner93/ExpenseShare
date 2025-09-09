package org.milad.expense_share.routing

import io.ktor.server.application.Application
import io.ktor.server.routing.routing

internal fun Application.routing() {
    routing {
        authRoutes()
        groupsRoutes()
        friendRoutes()
    }
}