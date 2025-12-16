package org.milad.expense_share.application

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.time.Duration.ofSeconds
import kotlin.time.toKotlinDuration

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = ofSeconds(15).toKotlinDuration()
        timeout = ofSeconds(15).toKotlinDuration()
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}