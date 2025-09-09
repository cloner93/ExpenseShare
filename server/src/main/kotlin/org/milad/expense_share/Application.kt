package org.milad.expense_share

import com.auth0.jwt.JWT
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.milad.expense_share.routing.routing
import org.slf4j.event.Level

fun Application.main() {
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> true }
    }

    configureSecurity()

    routing()
}

fun Application.configureSecurity() {

    JwtConfig.init(environment.config)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.getRealm()
            verifier(
                JWT
                    .require(JwtConfig.getAlgorithm())
                    .withIssuer(JwtConfig.getIssuer())
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("phone").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}