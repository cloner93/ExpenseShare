package plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException

fun HttpClientConfig<*>.installErrorHandler() {
    expectSuccess = true

    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, request ->
            when (exception) {
                is ClientRequestException -> {
                    val body = exception.response.bodyAsText()
                    if (exception.response.status == HttpStatusCode.NotFound) {
                        throw MissingPageException("Page not found: $body")
                    } else {
                        throw ApiException("Client error: ${exception.response.status}", exception)
                    }
                }

                is ServerResponseException -> {
                    throw ApiException("Server error: ${exception.response.status}", exception)
                }

                is RedirectResponseException -> {
                    throw ApiException("Redirect error: ${exception.response.status}", exception)
                }

                is ConnectTimeoutException,
                is SocketTimeoutException,
                is IOException -> {
                    throw NetworkException("Network error: ${exception.message}", exception)
                }

                else -> {
                    throw UnknownApiException("Unknown error: ${exception.message}", exception)
                }
            }
        }
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
class MissingPageException(message: String) : Exception(message)
class UnknownApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
