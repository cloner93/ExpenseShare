package usecase.friends

import fakes.FakeFriendsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.FriendInfo
import model.FriendRelationStatus
import model.User

class GetFriendRequestsUseCaseTest : StringSpec({
    val repo = FakeFriendsRepository()
    val useCase = GetIncomingRequestsUseCase(repo)

    "should return list of friend requests when successful" {
        repo.reset()
        val requests = listOf(
            FriendInfo(
                user = User(0, "milad", "09123456778"),
                status = FriendRelationStatus.PENDING,
                requestedBy = 0,
                createdAt = 0L,
                updatedAt = 0L
            ),
            FriendInfo(
                user = User(10, "mahdi", "09123456779"),
                status = FriendRelationStatus.PENDING,
                requestedBy = 10,
                createdAt = 0L,
                updatedAt = 0L
            )
        )

        repo.getIncomingRequestsResult = Result.success(requests)

        val result = useCase().first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe requests
        repo.getIncomingRequestsCallCount shouldBe 1
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.getIncomingRequestsResult = Result.failure(error)

        val result = useCase().first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        repo.getIncomingRequestsCallCount shouldBe 1
    }
})

