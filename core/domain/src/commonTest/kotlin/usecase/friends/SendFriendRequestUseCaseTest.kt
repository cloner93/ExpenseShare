package usecase.friends

import fakes.FakeFriendsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class SendFriendRequestUseCaseTest : StringSpec({
    val repo = FakeFriendsRepository()
    val useCase = SendFriendRequestUseCase(repo)
    val phone = "09131234556"

    "should return String when successful" {
        repo.reset()
        repo.sendFriendRequestResult = Result.success("Success")

        val result = useCase(phone).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.sendFriendRequestCallCount shouldBe 1
        repo.lastSendPhone shouldBe phone
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.sendFriendRequestResult = Result.failure(error)

        val result = useCase(phone).first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        repo.sendFriendRequestCallCount shouldBe 1
        repo.lastSendPhone shouldBe phone
    }
})