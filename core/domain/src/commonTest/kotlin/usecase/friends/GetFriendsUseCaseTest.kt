package usecase.friends

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.User
import repository.FriendsRepository

class GetFriendsUseCaseTest : StringSpec({

    val repo = mock(of<FriendsRepository>())

    val useCase = GetFriendsUseCase(repo)

    "should return list of friend when successful" {
        // Arrange
        val users = listOf(
            User(0, "milad", "09123456778"),
            User(10, "mahdi", "09123456779")
        )

        coEvery { repo.getFriends() }
            .returns(flowOf(Result.success(users)))

        // Act
        val result = useCase().first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe users
        coVerify { repo.getFriends() }.wasInvoked(exactly = 1)
    }

    "should return failure when repository fails" {
        val error = RuntimeException("network error")
        coEvery { repo.getFriends() }
            .returns(flowOf(Result.failure(error)))

        val result = useCase().first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        coVerify { repo.getFriends() }.wasInvoked(exactly = 1)
    }
})
