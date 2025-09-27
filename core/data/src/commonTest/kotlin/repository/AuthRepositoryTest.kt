package repository

import NetworkManager
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.LoginRequest
import model.RegisterRequest
import model.User

class AuthRepositoryTest : DescribeSpec({
    val network = mock(of<NetworkManager>())
    val repo = AuthRepositoryImpl(network)

    describe("register") {

        it("should return success when API succeeds") {
            // Arrange
            val req = RegisterRequest("phone", username = "Milad", "password")
            val user = User(1, "Milad", "phone")

            coEvery {
                network.post<RegisterRequest, User>("auth/register", req)
            } returns flowOf(Result.success(user))

            // Act
            val result = repo.register("phone", "Milad", "password").first()

            // Assert
            result.getOrNull() shouldNotBe null
            result.shouldBeSuccess {
                user.username shouldBe "Milad"
                user.phone shouldBe "phone"
            }
            coVerify {
                network.post<RegisterRequest, User>(
                    "auth/register",
                    req
                )
            }.wasInvoked(exactly = 1)
        }
        it("should return failure when API returns failure") {
            // Arrange
            val req = RegisterRequest("phone", username = "Milad", "password")
            val exception = RuntimeException("network error")

            coEvery {
                network.post<RegisterRequest, User>("auth/register", req)
            } returns flowOf(Result.failure(exception))

            // Act
            val result = repo.register("phone", "Milad", "password").first()

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            coVerify {
                network.post<RegisterRequest, User>(
                    "auth/register",
                    req
                )
            }.wasInvoked(exactly = 1)
        }
        it("should call network client with correct endpoint and request body") {
            // Arrange
            val req = RegisterRequest("phone", username = "Milad", "password")
            val user = User(1, "Milad", "phone")

            coEvery {
                network.post<RegisterRequest, User>("auth/register", req)
            } returns flowOf(Result.success(user))

            // Act
            repo.register("phone", "Milad", "password").first()

            // Assert
            coVerify {
                network.post<RegisterRequest, User>(
                    "auth/register",
                    req
                )
            }.wasInvoked(exactly = 1)
        }
    }
    describe("login") {
        it("should return success when API succeeds") {
            // Arrange
            val req = LoginRequest("phone", "password")
            val user = User(1, "Milad", "phone")

            coEvery {
                network.post<LoginRequest, User>("auth/login", req)
            } returns flowOf(Result.success(user))

            // Act
            val result = repo.login("phone", "password").first()

            // Assert
            result.getOrNull() shouldNotBe null
            result.shouldBeSuccess {
                user.username shouldBe "Milad"
                user.phone shouldBe "phone"
            }
            coVerify { network.post<LoginRequest, User>("auth/login", req) }.wasInvoked(exactly = 1)
        }
        it("should throw exception when API call fails") {
            // Arrange
            val req = LoginRequest("phone", "password")
            val error = RuntimeException("network error")

            coEvery {
                network.post<LoginRequest, User>("auth/login", req)
            } returns flowOf(Result.failure(error))

            // Act
            val result = repo.login("phone", "password").first()

            // Assert
            result.isSuccess shouldBe false
            result.exceptionOrNull() shouldBe error
            coVerify { network.post<LoginRequest, User>("auth/login", req) }.wasInvoked(exactly = 1)
        }
    }
})