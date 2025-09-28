package repository

import NetworkManager
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.LoginRequest
import model.RegisterRequest
import model.User

class AuthRepositoryTest : DescribeSpec({
    val network = mock(of<NetworkManager>())
    val repo = AuthRepositoryImpl(network)

    describe("register") {
        context("when API call succeeds") {
            it("should return success with user data") {
                // Arrange
                val req = RegisterRequest("phone", username = "Milad", "password")
                val expectedUser = User(1, "Milad", "phone")

                coEvery {
                    network.post<RegisterRequest, User>("auth/register", req)
                } returns flowOf(Result.success(expectedUser))

                // Act
                val result = repo.register("phone", "Milad", "password").first()

                // Assert
                result shouldBeSuccess { user ->
                    user.username shouldBe "Milad"
                    user.phone shouldBe "phone"
                    user.id shouldBe 1
                }

                coVerify {
                    network.post<RegisterRequest, User>("auth/register", req)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val req = RegisterRequest("phone", username = "Milad", "password")
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<RegisterRequest, User>("auth/register", req)
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.register("phone", "Milad", "password").first()

                // Assert
                result.shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<RegisterRequest, User>("auth/register", req)
                }.wasInvoked(exactly = 1)
            }
        }

        context("property-based testing") {
            it("should always call network with provided parameters") {
                checkAll<String, String, String> { phone, username, password ->
                    val req = RegisterRequest(phone, username, password)
                    val user = User(1, username, phone)

                    coEvery {
                        network.post<RegisterRequest, User>("auth/register", req)
                    } returns flowOf(Result.success(user))

                    repo.register(phone, username, password).first()

                    coVerify {
                        network.post<RegisterRequest, User>("auth/register", req)
                    }.wasInvoked(atLeast = 1)
                }
            }
        }
    }

    describe("login") {
        context("when API call succeeds") {
            it("should return success with user data") {
                // Arrange
                val req = LoginRequest("phone", "password")
                val expectedUser = User(1, "testuser", "phone")

                coEvery {
                    network.post<LoginRequest, User>("auth/login", req)
                } returns flowOf(Result.success(expectedUser))

                // Act
                val result = repo.login("phone", "password").first()

                // Assert
                result shouldBeSuccess { user ->
                    user.username shouldBe "testuser"
                    user.phone shouldBe "phone"
                    user.id shouldBe 1
                }

                coVerify {
                    network.post<LoginRequest, User>("auth/login", req)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val req = LoginRequest("phone", "password")
                val error = RuntimeException("network error")

                coEvery {
                    network.post<LoginRequest, User>("auth/login", req)
                } returns flowOf(Result.failure(error))

                // Act
                val result = repo.login("phone", "password").first()

                // Assert
                result.shouldBeFailure{ exception ->
                    exception shouldBe error
                }

                coVerify {
                    network.post<LoginRequest, User>("auth/login", req)
                }.wasInvoked(exactly = 1)
            }
        }
    }

    /*describe("authentication endpoints") {
        data class AuthTestCase(
            val operation: String,
            val endpoint: String,
            val repositoryCall: suspend (String, String, String) -> Flow<Result<User>>
        )

        val testData = listOf(
            AuthTestCase("register", "auth/register") { phone, username, password ->
                repo.register(phone, username, password)
            },
            AuthTestCase("login", "auth/login") { phone, _, password ->
                repo.login(phone, password)
            }
        )

        testData.forEach { testCase ->
            context("${testCase.operation} operation") {
                it("should use correct endpoint") {
                    val phone = "testphone"
                    val username = "testuser"
                    val password = "testpass"
                    val user = User(1, username, phone)

                    when (testCase.operation) {
                        "register" -> {
                            coEvery {
                                network.post<RegisterRequest, User>(
                                    testCase.endpoint,
                                    RegisterRequest(phone, username, password)
                                )
                            } returns flowOf(Result.success(user))
                        }

                        "login" -> {
                            coEvery {
                                network.post<LoginRequest, User>(
                                    testCase.endpoint,
                                    LoginRequest(phone, password)
                                )
                            } returns flowOf(Result.success(user))
                        }
                    }

                    testCase.repositoryCall(phone, username, password).first()

                    when (testCase.operation) {
                        "register" -> coVerify {
                            network.post<RegisterRequest, User>(
                                testCase.endpoint,
                                RegisterRequest(phone, username, password)
                            )
                        }.wasInvoked(exactly = 1)

                        "login" -> coVerify {
                            network.post<LoginRequest, User>(
                                testCase.endpoint,
                                LoginRequest(phone, password)
                            )
                        }.wasInvoked(exactly = 1)
                    }
                }
            }
        }
    }*/
})