package usecase.auth

import fakes.FakeAuthRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.User

class RegisterUserUseCaseTest : StringSpec({
    val repo = FakeAuthRepository()
    val useCase = RegisterUserUseCase(repo)

    "should return user on successful register" {
        repo.reset()
        val expectedUser = User(1, "milad", "09123456789")
        repo.registerResult = Result.success(expectedUser)

        val result = useCase("09123456789", "milad", "123456").first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe expectedUser
        repo.registerCallCount shouldBe 1
        repo.lastRegisterPhone shouldBe "09123456789"
        repo.lastRegisterUsername shouldBe "milad"
        repo.lastRegisterPassword shouldBe "123456"
    }

    "should return failure on error from repository" {
        repo.reset()
        val expectedException = RuntimeException("Register failed")
        repo.registerResult = Result.failure(expectedException)

        val result = useCase("09123456789", "milad", "123456").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
        repo.registerCallCount shouldBe 1
    }

    "should call repository with correct parameters" {
        repo.reset()
        useCase("09123456789", "milad", "123456").first()

        repo.registerCallCount shouldBe 1
        repo.lastRegisterPhone shouldBe "09123456789"
        repo.lastRegisterUsername shouldBe "milad"
        repo.lastRegisterPassword shouldBe "123456"
    }
})