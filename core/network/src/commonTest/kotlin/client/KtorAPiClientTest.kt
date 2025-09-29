package client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.path
import kotlinx.coroutines.test.runTest

class KtorAPiClientTest : DescribeSpec({
    val mockEngine = MockEngine { request ->
        respond(
            content = """{"message": "success"}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
        )
    }
    val mockHttpClient = HttpClient(mockEngine)
    val apiClient: ApiClient = KtorApiClient(mockHttpClient)

    describe("GET request") {
        it("should delegate to HttpClient and return response") {

            val response = apiClient.get {
                url { path("/get-test") }
                parameter("key", "value")
            }

            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldBe """{"message": "success"}"""

            val lastRequest = mockEngine.requestHistory.last()
            lastRequest.method shouldBe HttpMethod.Get
            lastRequest.url.encodedPath shouldBe "/get-test"
            lastRequest.url.parameters["key"] shouldBe "value"

        }
    }

    describe("POST request") {
        it("should delegate to HttpClient with body and headers") {
            runTest {
                val response = apiClient.post {
                    url { path("/post-test") }
                    setBody("""{"data": "post"}""")
                    header("Custom-Header", "value")
                }

                response.status shouldBe HttpStatusCode.OK

                val lastRequest = mockEngine.requestHistory.last()
                lastRequest.method shouldBe HttpMethod.Post
                lastRequest.url.encodedPath shouldBe "/post-test"
                lastRequest.headers["Custom-Header"] shouldBe "value"
                lastRequest.body.toByteArray().decodeToString() shouldBe """{"data": "post"}"""
            }
        }
    }

    describe("PUT request") {
        it("should delegate to HttpClient and handle body") {
            runTest {
                val response = apiClient.put {
                    url { path("/put-test") }
                    setBody("""{"update": "data"}""")
                }

                response.status shouldBe HttpStatusCode.OK

                val lastRequest = mockEngine.requestHistory.last()
                lastRequest.method shouldBe HttpMethod.Put
                lastRequest.url.encodedPath shouldBe "/put-test"
                lastRequest.body.toByteArray().decodeToString() shouldBe """{"update": "data"}"""
            }
        }
    }

    describe("DELETE request") {
        it("should delegate to HttpClient with parameters") {
            runTest {
                val response = apiClient.delete {
                    url { path("/delete-test") }
                    parameter("id", "123")
                }

                response.status shouldBe HttpStatusCode.OK

                val lastRequest = mockEngine.requestHistory.last()
                lastRequest.method shouldBe HttpMethod.Delete
                lastRequest.url.encodedPath shouldBe "/delete-test"
                lastRequest.url.parameters["id"] shouldBe "123"
            }
        }
    }

    xcontext("Concurrent Requests"){

    }
})