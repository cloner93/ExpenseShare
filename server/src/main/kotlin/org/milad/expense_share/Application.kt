package org.milad.expense_share

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.milad.expense_share.application.configureCORS
import org.milad.expense_share.application.configureKoin
import org.milad.expense_share.application.configureRouting
import org.milad.expense_share.application.configureSecurity
import org.milad.expense_share.application.configureStatusPages
import org.milad.expense_share.application.configureWebSockets
import org.milad.expense_share.data.db.DatabaseFactory
import org.slf4j.event.Level

fun Application.main() {
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> true }
    }

    DatabaseFactory.init()

    configureCORS()
    configureStatusPages()
    configureSecurity()
    configureWebSockets()
    configureKoin()
    configureRouting()
}