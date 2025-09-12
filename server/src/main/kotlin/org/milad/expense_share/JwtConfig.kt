package org.milad.expense_share

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import org.milad.expense_share.database.models.User
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var realm: String
    private var validityInMs: Long = 0

    private lateinit var algorithm: Algorithm

    fun init(config: ApplicationConfig) {

        secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        realm = config.property("jwt.realm").getString()
        validityInMs = config.property("jwt.validityMs").getString().toLong()

        algorithm = Algorithm.HMAC256(secret)
    }
    fun generateToken(user: User): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("id", user.id)
            .withClaim("username", user.username)
            .withClaim("phone", user.phone)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }
    fun getRealm() = realm
    fun getAlgorithm() = algorithm
    fun getIssuer() = issuer
}
