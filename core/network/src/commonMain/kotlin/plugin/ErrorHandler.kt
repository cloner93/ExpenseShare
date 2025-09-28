package plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

fun HttpClientConfig<*>.installErrorHandler() {
    expectSuccess = true

    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->

            if (exception !is ResponseException) {
                throw exception
            }

            val response = exception.response
            val statusCode = response.status.value
            val responseBody = try {
                response.bodyAsText()
            } catch (e: Exception) {
                ""
            }

            when (statusCode) {
                HttpStatusCode.Unauthorized.value -> throw UnauthorizedException(responseBody)
                HttpStatusCode.NotFound.value -> throw NotFoundException(responseBody)
                in 500..599 -> throw ServerException("Server error with status code: $statusCode")
                else -> throw GenericApiException(responseBody, statusCode)
            }
        }
    }
}

sealed class ApiException(message: String) : Exception(message)

class UnauthorizedException(message: String = "Authentication failed") : ApiException(message)
class NotFoundException(message: String = "Resource not found") : ApiException(message)
class ServerException(message: String = "Internal server error") : ApiException(message)
class GenericApiException(message: String, val code: Int) : ApiException("HTTP Error: $code - $message")
