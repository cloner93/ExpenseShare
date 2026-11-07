package client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest

class ApiClientTest : StringSpec({
    "should perform GET request and return OK response" {
        runTest {
            val mockEngine = MockEngine { _ ->
                respond("{}", HttpStatusCode.OK)
            }
            val apiClient = KtorApiClient(HttpClient(mockEngine))
            val response = apiClient.get {}
            response.status shouldBe HttpStatusCode.OK
        }
    }

    "should perform POST request and return Created response" {
        runTest {
            val mockEngine = MockEngine { _ ->
                respond("{}", HttpStatusCode.Created)
            }
            val apiClient = KtorApiClient(HttpClient(mockEngine))
            val response = apiClient.post {}
            response.status shouldBe HttpStatusCode.Created
        }
    }

    "should perform PUT request and verify invocation" {
        runTest {
            var invoked = false
            val mockEngine = MockEngine { _ ->
                invoked = true
                respond("{}", HttpStatusCode.OK)
            }
            val apiClient = KtorApiClient(HttpClient(mockEngine))
            val response = apiClient.put {}
            response.status shouldBe HttpStatusCode.OK
            invoked shouldBe true
        }
    }

    "should perform DELETE request and return NoContent" {
        runTest {
            val mockEngine = MockEngine { _ ->
                respond("", HttpStatusCode.NoContent)
            }
            val apiClient = KtorApiClient(HttpClient(mockEngine))
            val response = apiClient.delete {}
            response.status shouldBe HttpStatusCode.NoContent
        }
    }

    "should propagate network exceptions during request" {
        runTest {
            val mockEngine = MockEngine { _ ->
                throw ConnectTimeoutException("Network error")
            }
            val apiClient = KtorApiClient(HttpClient(mockEngine))
            val exception = shouldThrow<ConnectTimeoutException> {
                apiClient.get {}
            }
            exception.message shouldBe "Network error"
        }
    }
})