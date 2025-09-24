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

class SendFriendRequestUseCaseTest : StringSpec({

    val repo = mock(of<FriendsRepository>())
    val useCase = SendFriendRequestUseCase(repo)
    val phone = "09131234556"

    "should return unit when successful" {

        // Arrange
        coEvery { repo.sendFriendRequest(phone) }
            .returns(flowOf(Result.success(Unit)))

        // Act
        val result = useCase(phone).first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe Unit
        coVerify { repo.sendFriendRequest(phone) }.wasInvoked(exactly = 1)
    }

    "should return failure when repository fails" {

        val error = RuntimeException("network error")
        coEvery { repo.sendFriendRequest(phone) }
            .returns(flowOf(Result.failure(error)))

        val result = useCase(phone).first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        coVerify { repo.sendFriendRequest(phone) }.wasInvoked(exactly = 1)
    }
})