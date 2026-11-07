package usecase.friends

import fakes.FakeFriendsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class RemoveFriendUseCaseTest : StringSpec({
    val repo = FakeFriendsRepository()
    val useCase = RemoveFriendUseCase(repo)

    "should return String when successful" {
        repo.reset()
        repo.removeFriendResult = Result.success("Success")

        val result = useCase("09123456778").first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.removeFriendCallCount shouldBe 1
        repo.lastRemovePhone shouldBe "09123456778"
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.removeFriendResult = Result.failure(error)

        val result = useCase("09123456778").first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        repo.removeFriendCallCount shouldBe 1
        repo.lastRemovePhone shouldBe "09123456778"
    }
})

