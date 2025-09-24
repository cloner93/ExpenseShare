package usecase.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import model.User
import repository.AuthRepository

class LoginUserUseCaseTest : StringSpec({

    val repo = mock(of<AuthRepository>())
    val useCase = LoginUserUseCase(repo)

    "should return user on successful login" {
        // Arrange
        val expectedUser = User(1, "milad", "09123456789")
        coEvery { repo.login("09123456789", "123456") }
            .returns(flowOf(Result.success(expectedUser)))

        // Act
        val result = useCase("09123456789", "123456").first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe expectedUser
        coVerify { repo.login("09123456789", "123456") }
            .wasInvoked(atLeast = 1)
    }

    "should return failure on login error" {
        // Arrange
        val expectedException = RuntimeException("Login failed")
        coEvery { repo.login("09123456789", "123456") }
            .returns(flowOf(Result.failure(expectedException)))

        // Act
        val result = useCase("09123456789", "123456").first()

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    "should call repository with correct parameters" {
        // Arrange
        coEvery { repo.login(any(), any()) } returns flow {
            emit(Result.success(User(1, "milad", "09123456789")))
        }

        // Act
        useCase("09123456789", "123456")

        // Assert
        coVerify { repo.login("09123456789", "123456") }
            .wasInvoked(atLeast = 1)
    }
})
