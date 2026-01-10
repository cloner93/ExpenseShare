package org.milad.expense_share.application

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.milad.expense_share.presentation.api_model.ErrorResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Internal server error",
                    code = "INTERNAL_ERROR",
                    details = mapOf("error" to cause.localizedMessage)
                )
            )
            cause.printStackTrace()
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse("Resource not found", "NOT_FOUND")
            )
        }
    }
}