package client

import getKtorEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import plugin.installErrorHandler
import token.TokenProvider

data class HttpConfig(
    val baseUrl: String = "http://localhost:8080",
    val timeoutMillis: Long = 15000,
    val isDebug: Boolean = true,
    val refreshTokenEndpoint: String = "/auth/refresh" // FIXME
)

fun createHttpClient(
    tokenProvider: TokenProvider,
    config: HttpConfig = HttpConfig(),
    engine: HttpClientEngine? = null
) = HttpClient(engine = engine ?: getKtorEngine()) {
    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)

        tokenProvider.loadTokens()?.let {
            header(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = config.isDebug
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = config.timeoutMillis
        connectTimeoutMillis = config.timeoutMillis
        socketTimeoutMillis = config.timeoutMillis
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                print("ktor: $message")
            }
        }
        level = LogLevel.ALL
    }

    // remove handle token
    /*  install(Auth) {
          bearer {
              loadTokens { tokenProvider.loadTokens() }
              refreshTokens { null }
          }
      }*/

    installErrorHandler()
}