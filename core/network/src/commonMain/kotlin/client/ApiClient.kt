package client

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.mockative.Mockable

@Mockable
interface ApiClient {
    suspend fun post(builder: HttpRequestBuilder.() -> Unit): HttpResponse
    suspend fun get(builder: HttpRequestBuilder.() -> Unit): HttpResponse
    suspend fun put(builder: HttpRequestBuilder.() -> Unit): HttpResponse
    suspend fun delete(builder: HttpRequestBuilder.() -> Unit): HttpResponse
}