package org.milad.expense_share.application

import com.auth0.jwt.JWT
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.milad.expense_share.security.JwtConfig

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