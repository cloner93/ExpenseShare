package usecase.friends

import fakes.FakeFriendsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.FriendInfo
import model.FriendRelationStatus
import model.User

class GetFriendsUseCaseTest : StringSpec({
    val repo = FakeFriendsRepository()
    val useCase = GetAllFriendsUseCase(repo)

    "should return list of friend when successful" {
        repo.reset()
        val friends = listOf(
            FriendInfo(
                user = User(0, "milad", "09123456778"),
                status = FriendRelationStatus.ACCEPTED,
                requestedBy = 0,
                createdAt = 0L,
                updatedAt = 0L
            ),
            FriendInfo(
                user = User(10, "mahdi", "09123456779"),
                status = FriendRelationStatus.ACCEPTED,
                requestedBy = 10,
                createdAt = 0L,
                updatedAt = 0L
            )
        )

        repo.getAllFriendsResult = Result.success(friends)

        val result = useCase().first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe friends
        repo.getAllFriendsCallCount shouldBe 1
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.getAllFriendsResult = Result.failure(error)

        val result = useCase().first()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe error
        repo.getAllFriendsCallCount shouldBe 1
    }
})

