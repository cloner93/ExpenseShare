package usecase.friends

import fakes.FakeFriendsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class AcceptFriendRequestUseCaseTest : StringSpec({
    val repo = FakeFriendsRepository()
    val useCase = AcceptFriendRequestUseCase(repo)

    "should succeed when friend request is accepted" {
        repo.reset()
        repo.acceptFriendRequestResult = Result.success("Success")

        val result = useCase("0912000000").first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.acceptFriendRequestCallCount shouldBe 1
        repo.lastAcceptPhone shouldBe "0912000000"
    }

    "should fail when accepting friend request fails" {
        repo.reset()
        val error = RuntimeException("accept failed")
        repo.acceptFriendRequestResult = Result.failure(error)

        val result = useCase("0912000000").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        repo.acceptFriendRequestCallCount shouldBe 1
        repo.lastAcceptPhone shouldBe "0912000000"
    }
})

