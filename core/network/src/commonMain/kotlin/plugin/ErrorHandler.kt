package plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ApiErrorPayload(
    val message: String? = null,
    val error: String? = null,
)

private val errorJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private val htmlMarkerRegex = Regex(
    pattern = """(?i)(<!doctype\s+html|<\s*html\b|<\s*head\b|<\s*body\b|<\s*title\b|<\s*script\b|<\s*style\b)""",
)

private val htmlTagRegex = Regex("""<[^>]+>""")
private val whitespaceRegex = Regex("""\s+""")

fun HttpClientConfig<*>.installErrorHandler() {
    expectSuccess = true

    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->

            if (exception !is ResponseException) {
                throw exception
            }

            val response = exception.response
            val statusCode = response.status.value
            val responseContentType = response.contentType()
            val responseBody = try {
                response.bodyAsText()
            } catch (_: Exception) {
                ""
            }

            val safeMessage = sanitizeErrorMessage(
                statusCode = statusCode,
                contentType = responseContentType,
                rawBody = responseBody,
            )

            when (statusCode) {
                HttpStatusCode.Unauthorized.value -> throw UnauthorizedException(safeMessage)
                HttpStatusCode.NotFound.value -> throw NotFoundException(safeMessage)
                in 500..599 -> throw ServerException("Server error with status code: $statusCode")
                else -> throw GenericApiException(safeMessage, statusCode)
            }
        }
    }
}

/**
 * Extracts a clean user-facing error message from HTTP error bodies while
 * preventing raw HTML pages (e.g. reverse-proxy 405/502/503 responses) or raw JSON
 * payloads from leaking directly into the UI.
 */
internal fun sanitizeErrorMessage(
    statusCode: Int,
    contentType: ContentType?,
    rawBody: String,
): String {
    val trimmed = rawBody.trim()
    if (trimmed.isEmpty()) {
        return defaultStatusMessage(statusCode)
    }

    val isHtml = contentType?.match(ContentType.Text.Html) == true ||
        htmlMarkerRegex.containsMatchIn(trimmed)
    if (isHtml) {
        return defaultStatusMessage(statusCode)
    }

    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        val parsed = runCatching {
            errorJson.decodeFromString<ApiErrorPayload>(trimmed)
        }.getOrNull()
        val jsonMessage = parsed?.message?.takeIf { it.isNotBlank() }
            ?: parsed?.error?.takeIf { it.isNotBlank() }
        if (jsonMessage != null) {
            return stripHtmlTags(jsonMessage).ifBlank { defaultStatusMessage(statusCode) }
        }
    }

    val stripped = stripHtmlTags(trimmed)
    return if (stripped.isBlank()) {
        defaultStatusMessage(statusCode)
    } else if (stripped.length > 240) {
        stripped.take(240).trimEnd() + "..."
    } else {
        stripped
    }
}

private fun stripHtmlTags(text: String): String =
    text.replace(htmlTagRegex, " ")
        .replace(whitespaceRegex, " ")
        .trim()

private fun defaultStatusMessage(statusCode: Int): String = when (statusCode) {
    HttpStatusCode.BadRequest.value -> "Invalid request"
    HttpStatusCode.Unauthorized.value -> "Authentication failed"
    HttpStatusCode.Forbidden.value -> "Access denied"
    HttpStatusCode.NotFound.value -> "Resource not found"
    HttpStatusCode.MethodNotAllowed.value -> "Method not allowed"
    HttpStatusCode.RequestTimeout.value -> "Request timed out"
    HttpStatusCode.TooManyRequests.value -> "Too many requests. Please try again later."
    in 500..599 -> "Server error with status code: $statusCode"
    else -> "Request failed with status $statusCode"
}

sealed class ApiException(message: String) : Exception(message)

class UnauthorizedException(message: String = "Authentication failed") : ApiException(message)
class NotFoundException(message: String = "Resource not found") : ApiException(message)
class ServerException(message: String = "Internal server error") : ApiException(message)
class GenericApiException(message: String, val code: Int) : ApiException("HTTP Error: $code - $message")
