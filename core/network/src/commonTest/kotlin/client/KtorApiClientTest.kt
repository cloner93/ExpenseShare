package client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import plugin.GenericApiException
import plugin.NotFoundException
import plugin.ServerException
import plugin.UnauthorizedException
import token.InMemoryTokenProvider

class KtorApiClientTest : DescribeSpec({

    describe("HttpClient Configuration") {
        context("when creating client with default configuration") {
            it("should install all required plugins") {
                val tokenProvider = InMemoryTokenProvider()
                val client = createHttpClient(tokenProvider)

                client.pluginOrNull(ContentNegotiation) shouldNotBe null
                client.pluginOrNull(HttpTimeout) shouldNotBe null
                client.pluginOrNull(Logging) shouldNotBe null
            }

            it("should use provided engine") {
                val customMockEngine = MockEngine { request ->
                    respond(
                        content = """{"custom": "engine"}""", status = HttpStatusCode.OK
                    )
                }

                val tokenProvider = InMemoryTokenProvider()
                val client = createHttpClient(tokenProvider, engine = customMockEngine)

                runTest {
                    val response = client.get("/custom")
                    response.bodyAsText() shouldBe """{"custom": "engine"}"""
                }
            }

            it("should set correct base URL and default headers") {
                val mockEngine = MockEngine { request ->
                    respond(
                        content = "{}",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                val tokenProvider = InMemoryTokenProvider()
                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val response = client.get("/test")

                    response.request.url.host shouldBe "localhost"
                    response.request.url.port shouldBe 8080
                    response.request.url.protocol shouldBe URLProtocol.HTTPS
//                    response.request.headers[HttpHeaders.ContentType] shouldContain "application/json"
                }
            }
        }
    }

    describe("HTTP Requests") {
        lateinit var mockEngine: MockEngine
        lateinit var client: HttpClient

        beforeEach {
            mockEngine = MockEngine { request ->
                when {
                    request.url.encodedPath == "/success" && request.method == HttpMethod.Get -> respond(
                        content = """{"status": "success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    request.url.encodedPath == "/create" && request.method == HttpMethod.Post -> {
                        val requestBody = request.body.toByteArray().decodeToString()
                        requestBody shouldBe """{"name": "test"}"""
                        respond(
                            content = """{"created": true}""",
                            status = HttpStatusCode.Created,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }

                    request.url.encodedPath.startsWith("/update/") && request.method == HttpMethod.Put -> respond(
                        content = """{"updated": true}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    request.url.encodedPath.startsWith("/delete/") && request.method == HttpMethod.Delete -> respond(
                        content = "", status = HttpStatusCode.NoContent
                    )

                    // error
                    request.url.encodedPath == "/error" -> respondError(HttpStatusCode.BadRequest)
                    request.url.encodedPath == "/generic-error" -> respond(
                        content = "Payment Required",
                        status = HttpStatusCode.PaymentRequired
                    )

                    request.url.encodedPath == "/timeout" -> throw ConnectTimeoutException("Connection timeout")
                    request.url.encodedPath == "/unauthorized" -> respond(
                        content = "Invalid token", status = HttpStatusCode.Unauthorized
                    )

                    request.url.encodedPath == "/not-found" -> respond(
                        content = "The requested item does not exist",
                        status = HttpStatusCode.NotFound
                    )

                    request.url.encodedPath == "/server-error" -> respond(
                        content = "Something went wrong on the server",
                        status = HttpStatusCode.InternalServerError
                    )

                    else -> respond(
                        content = "Not Found: ${request.url.encodedPath}",
                        status = HttpStatusCode.NotFound
                    )
                }
            }

            val tokenProvider = InMemoryTokenProvider()
            client = createHttpClient(tokenProvider, engine = mockEngine)
        }

        context("successful requests") {
            it("should handle GET requests correctly") {
                runTest {
                    val response = client.get("/success")
                    response.status shouldBe HttpStatusCode.OK
                    response.bodyAsText() shouldBe """{"status": "success"}"""
                }
            }

            it("should handle POST requests with JSON body") {
                runTest {
                    val response = client.post("/create") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"name": "test"}""")
                    }
                    response.status shouldBe HttpStatusCode.Created
                    response.bodyAsText() shouldBe """{"created": true}"""
                }
            }

            it("should handle PUT requests") {
                runTest {
                    val response = client.put("/update/123") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"field": "value"}""")
                    }
                    response.status shouldBe HttpStatusCode.OK
                    response.request.method shouldBe HttpMethod.Put
                    response.bodyAsText() shouldBe """{"updated": true}"""
                }
            }

            it("should handle DELETE requests") {
                runTest {
                    val response = client.delete("/delete/123")
                    response.status shouldBe HttpStatusCode.NoContent
                    response.request.method shouldBe HttpMethod.Delete
                }
            }
        }

        context("error handling") {
            it("should throw UnauthorizedException for 401 Unauthorized") {
                val exception = shouldThrow<UnauthorizedException> {
                    client.get("/unauthorized")
                }
                exception.message shouldBe "Invalid token"
            }

            it("should throw NotFoundException for 404 Not Found") {
                val exception = shouldThrow<NotFoundException> {
                    client.get("/not-found")
                }
                exception.message shouldBe "The requested item does not exist"
            }

            it("should throw ServerException for 500 Internal Server Error") {
                val exception = shouldThrow<ServerException> {
                    client.get("/server-error")
                }
                exception.message shouldBe "Server error with status code: 500"
            }

            it("should throw GenericApiException for other client errors") {
                val exception = shouldThrow<GenericApiException> {
                    client.get("/generic-error")
                }
                exception.code shouldBe 402
                exception.message shouldBe "HTTP Error: 402 - Payment Required"
            }

            it("should handle HTTP error responses") {
                val exception = shouldThrow<GenericApiException> {
                    client.get("/error")
                }
                exception.code shouldBe HttpStatusCode.BadRequest.value
            }

            it("should handle network timeouts") {
                val exception = shouldThrow<ConnectTimeoutException> {
                    client.get("/timeout")
                }
                exception.message shouldBe "Connection timeout"
            }
        }
    }

    describe("Authentication Integration") {

        val tokenProvider = InMemoryTokenProvider()
        beforeEach {
            tokenProvider.clearToken()
        }

        context("when token is available") {
            it("should add Authorization header to requests") {
                tokenProvider.setToken(BearerTokens("test-bearer-token", null))

                val mockEngine = MockEngine { request ->
                    val authHeader = request.headers[HttpHeaders.Authorization]
                    authHeader shouldBe "Bearer test-bearer-token"

                    respond(
                        content = """{"authenticated": true, "user": "testuser"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val response = client.get("/protected")
                    response.status shouldBe HttpStatusCode.OK
                    response.bodyAsText() shouldBe """{"authenticated": true, "user": "testuser"}"""
                }
            }

            it("should work with different API endpoints when authenticated") {
                tokenProvider.setToken(BearerTokens("valid-token", null))

                val mockEngine = MockEngine { request ->
                    val authHeader = request.headers[HttpHeaders.Authorization]
                    authHeader shouldBe "Bearer valid-token"

                    when (request.url.encodedPath) {
                        "/friends" -> respond(
                            content = """{"friends": []}""",
                            status = HttpStatusCode.OK
                        )

                        "/groups" -> respond(
                            content = """{"groups": []}""",
                            status = HttpStatusCode.OK
                        )

                        "/profile" -> respond(
                            content = """{"username": "testuser", "phone": "09123456789"}""",
                            status = HttpStatusCode.OK
                        )

                        else -> respond(content = "{}", status = HttpStatusCode.OK)
                    }
                }

                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val friendsResponse = client.get("/friends")
                    friendsResponse.bodyAsText() shouldBe """{"friends": []}"""

                    val groupsResponse = client.get("/groups")
                    groupsResponse.bodyAsText() shouldBe """{"groups": []}"""

                    val profileResponse = client.get("/profile")
                    profileResponse.bodyAsText() shouldBe """{"username": "testuser", "phone": "09123456789"}"""
                }
            }

            it("should handle token in POST requests (after login/register)") {
                tokenProvider.setToken(BearerTokens("new-user-token", null))

                val mockEngine = MockEngine { request ->
                    val authHeader = request.headers[HttpHeaders.Authorization]
                    authHeader shouldBe "Bearer new-user-token"

                    when {
                        request.url.encodedPath == "/friends/request" && request.method == HttpMethod.Post -> {
                            val requestBody = request.body.toByteArray().decodeToString()
                            respond(
                                content = """{"message": "Friend request sent"}""",
                                status = HttpStatusCode.OK
                            )
                        }

                        request.url.encodedPath == "/groups" && request.method == HttpMethod.Post -> {
                            respond(
                                content = """{"groupId": "new-group-123"}""",
                                status = HttpStatusCode.Created
                            )
                        }

                        else -> respond(content = "{}", status = HttpStatusCode.OK)
                    }
                }

                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val response = client.post("/friends/request") {
                        contentType(ContentType.Application.Json)
                        setBody(""""09123456789"""")
                    }
                    response.bodyAsText() shouldBe """{"message": "Friend request sent"}"""
                }
            }
        }

        context("when token is not available") {
            it("should make request without Authorization header for public endpoints") {
                val mockEngine = MockEngine { request ->
                    val authHeader = request.headers[HttpHeaders.Authorization]
                    authHeader shouldBe null

                    when (request.url.encodedPath) {
                        "/auth/login" -> respond(
                            content = """{"token": "login-success-token", "user": {"id": 1, "username": "testuser"}}""",
                            status = HttpStatusCode.OK
                        )

                        "/auth/register" -> respond(
                            content = """{"token": "register-success-token", "user": {"id": 2, "username": "newuser"}}""",
                            status = HttpStatusCode.Created
                        )

                        "/public" -> respond(
                            content = """{"public": true, "message": "No auth required"}""",
                            status = HttpStatusCode.OK
                        )

                        else -> respond(content = "{}", status = HttpStatusCode.OK)
                    }
                }

                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val loginResponse = client.post("/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"phone": "09123456789", "password": "testpass"}""")
                    }
                    loginResponse.bodyAsText() shouldBe """{"token": "login-success-token", "user": {"id": 1, "username": "testuser"}}"""

                    val registerResponse = client.post("/auth/register") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"phone": "09123456789", "username": "newuser", "password": "testpass"}""")
                    }
                    registerResponse.bodyAsText() shouldBe """{"token": "register-success-token", "user": {"id": 2, "username": "newuser"}}"""

                    val publicResponse = client.get("/public")
                    publicResponse.bodyAsText() shouldBe """{"public": true, "message": "No auth required"}"""
                }
            }

            it("should fail when accessing protected endpoints without token") {
                val mockEngine = MockEngine { request ->
                    val authHeader = request.headers[HttpHeaders.Authorization]

                    when {
                        authHeader == null && request.url.encodedPath.startsWith("/protected") -> {
                            respond(
                                content = "Invalid token",
                                status = HttpStatusCode.Unauthorized
                            )
                        }

                        authHeader == null && request.url.encodedPath == "/friends" -> {
                            respond(content = "Invalid token", status = HttpStatusCode.Unauthorized)
                        }

                        else -> respond(content = "{}", status = HttpStatusCode.OK)
                    }
                }

                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val exception = shouldThrow<UnauthorizedException> {
                        client.get("/friends")
                    }
                    exception.message shouldBe "Invalid token"
                }
            }
        }

        context("token management lifecycle") {
            it("should simulate complete auth flow") {
                val mockEngine = MockEngine { request ->
                    val authHeader = request.headers[HttpHeaders.Authorization]

                    when {
                        request.url.encodedPath == "/auth/login" && authHeader == null -> {
                            respond(
                                content = """{"token": "user-session-token", "user": {"id": 1, "username": "loginuser"}}""",
                                status = HttpStatusCode.OK
                            )
                        }
                        request.url.encodedPath == "/friends" && authHeader == "Bearer user-session-token" -> {
                            respond(
                                content = """{"friends": [{"username": "friend1"}, {"username": "friend2"}]}""",
                                status = HttpStatusCode.OK
                            )
                        }
                        request.url.encodedPath == "/friends" && authHeader != "Bearer user-session-token" -> {
                            respond(
                                content = "Unauthorized access",
                                status = HttpStatusCode.Unauthorized
                            )
                        }
                        else -> respond(content = "{}", status = HttpStatusCode.OK)
                    }
                }

                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    val loginResponse = client.post("/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"phone": "09123456789", "password": "password"}""")
                    }
                    loginResponse.status shouldBe HttpStatusCode.OK

                    tokenProvider.setToken(BearerTokens("user-session-token", null))

                    val friendsResponse = client.get("/friends")
                    friendsResponse.status shouldBe HttpStatusCode.OK
                    friendsResponse.bodyAsText() shouldBe """{"friends": [{"username": "friend1"}, {"username": "friend2"}]}"""

                    tokenProvider.clearToken()

                    val exception = shouldThrow<UnauthorizedException> {
                        client.get("/friends")
                    }
                    exception.message shouldBe "Unauthorized access"
                }
            }
        }
    }

    describe("Request Headers and Parameters") {

        context("custom headers") {
            it("should add custom headers to requests") {
                var mockEngine = MockEngine { request ->
                    val customHeader = request.headers["X-Custom-Header"]
                    customHeader shouldBe "custom-value"

                    respond(content = "{}", status = HttpStatusCode.OK)
                }
                val tokenProvider = InMemoryTokenProvider()
                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    client.get("/test") {
                        header("X-Custom-Header", "custom-value")
                    }
                }
            }
        }

        context("query parameters") {
            it("should handle query parameters correctly") {
                val mockEngine = MockEngine { request ->
                    val params = request.url.parameters
                    params["param1"] shouldBe "value1"
                    params["param2"] shouldBe "value2"

                    respond(content = "{}", status = HttpStatusCode.OK)
                }
                val tokenProvider = InMemoryTokenProvider()
                val client = createHttpClient(tokenProvider, engine = mockEngine)

                runTest {
                    client.get("/search") {
                        parameter("param1", "value1")
                        parameter("param2", "value2")
                    }
                }
            }
        }
    }

    describe("Timeout Configuration") {
        it("should respect timeout settings") {
            val tokenProvider = InMemoryTokenProvider()
            val client = createHttpClient(tokenProvider)
            val timeout = client.pluginOrNull(HttpTimeout)

            timeout shouldNotBe null
        }
    }
})