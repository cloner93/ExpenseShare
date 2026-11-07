package repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import model.User
import token.InMemoryTokenProvider

class AuthRepositoryTest : DescribeSpec({
    describe("register") {
        context("when API call succeeds") {
            it("should return success with user data and persist token and user session") {
                val tokenProvider = InMemoryTokenProvider()
                val userRepository = UserRepositoryImpl(InMemoryUserStorage())
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "auth/register"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": {
                                    "token": "fake-jwt-token",
                                    "user": {
                                        "id": 1,
                                        "username": "Milad",
                                        "phone": "09123456789"
                                    }
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = AuthRepositoryImpl(network, tokenProvider, userRepository)

                val result = repo.register("09123456789", "Milad", "password").first()

                result shouldBeSuccess { user ->
                    user.username shouldBe "Milad"
                    user.phone shouldBe "09123456789"
                    user.id shouldBe 1
                }
                tokenProvider.loadTokens()?.accessToken shouldBe "fake-jwt-token"
                userRepository.getInfo() shouldBe User(1, "Milad", "09123456789")
            }
        }

        context("when API call fails") {
            it("should return failure and clear tokens") {
                val tokenProvider = InMemoryTokenProvider()
                tokenProvider.setToken("stale-token")
                val userRepository = UserRepositoryImpl(InMemoryUserStorage())
                val network = createTestNetworkManager { _ ->
                    respond(
                        content = """{"success": false, "data": null}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = AuthRepositoryImpl(network, tokenProvider, userRepository)

                val result = repo.register("09123456789", "Milad", "wrong").first()

                result.shouldBeFailure()
                tokenProvider.loadTokens() shouldBe null
            }
        }
    }

    describe("login") {
        context("when API call succeeds") {
            it("should return success with user data and persist token and user session") {
                val tokenProvider = InMemoryTokenProvider()
                val userRepository = UserRepositoryImpl(InMemoryUserStorage())
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "auth/login"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": {
                                    "token": "login-jwt-token",
                                    "user": {
                                        "id": 2,
                                        "username": "testuser",
                                        "phone": "09987654321"
                                    }
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = AuthRepositoryImpl(network, tokenProvider, userRepository)

                val result = repo.login("09987654321", "password").first()

                result shouldBeSuccess { user ->
                    user.username shouldBe "testuser"
                    user.phone shouldBe "09987654321"
                    user.id shouldBe 2
                }
                tokenProvider.loadTokens()?.accessToken shouldBe "login-jwt-token"
                userRepository.getInfo() shouldBe User(2, "testuser", "09987654321")
            }
        }

        context("when API call fails") {
            it("should return failure with exception and clear token") {
                val tokenProvider = InMemoryTokenProvider()
                tokenProvider.setToken("stale-token")
                val userRepository = UserRepositoryImpl(InMemoryUserStorage())
                val network = createTestNetworkManager { _ ->
                    respond(
                        content = """{"success": false, "data": null}""",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = AuthRepositoryImpl(network, tokenProvider, userRepository)

                val result = repo.login("09987654321", "wrongpass").first()

                result.shouldBeFailure()
                tokenProvider.loadTokens() shouldBe null
            }
        }
    }
})