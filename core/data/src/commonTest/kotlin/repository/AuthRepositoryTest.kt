package repository

import NetworkManager
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockative.any
import io.mockative.coEvery
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.LoginRequest
import model.User

class AuthRepositoryTest : StringSpec({
    val network = mock(of<NetworkManager>())
    val repo = AuthRepositoryImpl(network)

    "login should return success when API succeeds" {
        // Arrange
        val req = LoginRequest("phone", "password")
        val user = User(1, "testuser", "phone")

        coEvery { network.post<LoginRequest, User>("auth/login", req) }
            .returns(flowOf(Result.success(user)))

        val result = repo.login("", "").first()

        result.getOrNull() shouldNotBe null
        result.getOrNull()?.username shouldBe "testuser"
    }
})