package usecase.auth

import fakes.FakeAuthRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.User

class LoginUserUseCaseTest : StringSpec({
    val repo = FakeAuthRepository()
    val useCase = LoginUserUseCase(repo)

    "should return user on successful login" {
        repo.reset()
        val expectedUser = User(1, "milad", "09123456789")
        repo.loginResult = Result.success(expectedUser)

        val result = useCase("09123456789", "123456").first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe expectedUser
        repo.loginCallCount shouldBe 1
        repo.lastLoginPhone shouldBe "09123456789"
        repo.lastLoginPassword shouldBe "123456"
    }

    "should return failure on login error" {
        repo.reset()
        val expectedException = RuntimeException("Login failed")
        repo.loginResult = Result.failure(expectedException)

        val result = useCase("09123456789", "123456").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
        repo.loginCallCount shouldBe 1
    }

    "should call repository with correct parameters" {
        repo.reset()
        useCase("09123456789", "123456").first()

        repo.loginCallCount shouldBe 1
        repo.lastLoginPhone shouldBe "09123456789"
        repo.lastLoginPassword shouldBe "123456"
    }
})

