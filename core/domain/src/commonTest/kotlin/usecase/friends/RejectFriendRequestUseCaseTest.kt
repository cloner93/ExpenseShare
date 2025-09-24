package usecase.friends

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import repository.FriendsRepository

class RejectFriendRequestUseCaseTest : StringSpec({

    val repo = mock(of<FriendsRepository>())

    val useCase = RejectFriendRequestUseCase(repo)

    "should return Unit when successful" {
        // Arrange

        coEvery { repo.rejectFriendRequest("09123456778") }
            .returns(flowOf(Result.success(Unit)))

        // Act
        val result = useCase("09123456778").first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe Unit
        coVerify { repo.rejectFriendRequest("09123456778") }.wasInvoked(exactly = 1)
    }

    "should return failure when repository fails" {
        val error = RuntimeException("network error")
        coEvery { repo.rejectFriendRequest("09123456778") }
            .returns(flowOf(Result.failure(error)))

        val result = useCase("09123456778").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        coVerify { repo.rejectFriendRequest("09123456778") }.wasInvoked(exactly = 1)
    }
})
