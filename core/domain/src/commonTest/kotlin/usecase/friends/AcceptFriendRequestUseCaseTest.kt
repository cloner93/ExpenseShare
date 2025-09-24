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

class AcceptFriendRequestUseCaseTest : StringSpec({

    val repo = mock(of<FriendsRepository>())
    val useCase = AcceptFriendRequestUseCase(repo)

    "should succeed when friend request is accepted" {
        // Arrange
        coEvery { repo.acceptFriendRequest("0912000000") } returns flowOf(Result.success(Unit))

        // Act
        val result = useCase("0912000000").first()

        // Assert
        result.isSuccess shouldBe true
        coVerify { repo.acceptFriendRequest("0912000000") }
    }

    "should fail when accepting friend request fails" {
        val error = RuntimeException("accept failed")

        coEvery { repo.acceptFriendRequest("0912000000") } returns flowOf(Result.failure(error))

        val result = useCase("0912000000").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        coVerify { repo.acceptFriendRequest("0912000000") }
    }
})
