package client

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse

class KtorApiClient(private val httpClient: HttpClient) : ApiClient {
    override suspend fun post(builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return httpClient.post(builder)
    }

    override suspend fun get(builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return httpClient.get(builder)
    }

    override suspend fun put(builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return httpClient.put(builder)
    }

    override suspend fun delete(builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return httpClient.delete(builder)
    }
}

