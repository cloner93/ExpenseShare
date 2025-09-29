package client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockative.Mockable
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.every
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest


class ApiClientTest : StringSpec() {
    private val apiClient = mock(of<ApiClient>())
    private val res = mock(of<HttpResponse>())

    init {
        "should mock POST and return custom response" {
            runTest {

                every { res.status }.returns(HttpStatusCode.OK)

                coEvery { apiClient.post(any<HttpRequestBuilder.() -> Unit>()) }
                    .returns(res)

                val response = apiClient.post { /* builder mock */ }
                response.status shouldBe HttpStatusCode.OK
            }
        }

        "should mock GET and simulate failure" {
            runTest {
                coEvery { apiClient.get(any()) } throws Exception("Network error")

                shouldThrow<Exception> {
                    apiClient.get { /* builder */ }
                }.message shouldBe "Network error"
            }
        }

        "should verify PUT call with builder" {
            runTest {
                every { res.status }.returns(HttpStatusCode.OK)

                coEvery { apiClient.put(any<HttpRequestBuilder.() -> Unit>()) }
                    .returns(res)

                apiClient.put { /* simulate builder */ }

                coVerify { apiClient.put(any()) }.wasInvoked(exactly = 1)
            }
        }

        "should stub DELETE and check invocation" {
            runTest {

                every { res.status }.returns(HttpStatusCode.NoContent)

                coEvery { apiClient.delete(any<HttpRequestBuilder.() -> Unit>()) }
                    .returns(res)

                val response = apiClient.delete { /* builder */ }
                response.status shouldBe HttpStatusCode.NoContent

                coVerify { apiClient.delete(any()) }.wasInvoked(1)
            }
        }
    }
}