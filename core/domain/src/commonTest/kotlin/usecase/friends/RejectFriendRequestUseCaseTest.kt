package usecase.friends

import fakes.FakeFriendsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class RejectFriendRequestUseCaseTest : StringSpec({
    val repo = FakeFriendsRepository()
    val useCase = RejectFriendRequestUseCase(repo)

    "should return String when successful" {
        repo.reset()
        repo.rejectFriendRequestResult = Result.success("Success")

        val result = useCase("09123456778").first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.rejectFriendRequestCallCount shouldBe 1
        repo.lastRejectPhone shouldBe "09123456778"
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.rejectFriendRequestResult = Result.failure(error)

        val result = useCase("09123456778").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        repo.rejectFriendRequestCallCount shouldBe 1
        repo.lastRejectPhone shouldBe "09123456778"
    }
})

