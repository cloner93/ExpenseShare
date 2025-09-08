package org.milad.expense_share

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "super_secret_jwt_key"
    private const val issuer = "expense-share-server"
    private const val audience = "expense-share-client"
    private const val validityInMs = 36_000_00 * 10

    private val algorithm = Algorithm.HMAC256(secret)

    fun makeToken(phone: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("phone", phone)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }

    fun getAlgorithm() = algorithm
    fun getIssuer() = issuer
    fun getAudience() = audience
}
