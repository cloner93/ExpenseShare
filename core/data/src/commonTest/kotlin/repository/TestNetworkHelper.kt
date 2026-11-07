package repository

import NetworkManager
import client.KtorApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createTestNetworkManager(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
): NetworkManager {
    val client = HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        defaultRequest {
            url("http://127.0.0.1:8082/")
            contentType(ContentType.Application.Json)
        }
    }
    return NetworkManager(KtorApiClient(client))
}
