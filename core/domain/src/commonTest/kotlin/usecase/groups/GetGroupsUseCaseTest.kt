package usecase.groups

import fakes.FakeGroupsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.Group

class GetGroupsUseCaseTest : StringSpec({
    val repo = FakeGroupsRepository()
    val usecase = GetGroupsUseCase(repo)

    "should return groups when repository succeeds" {
        repo.reset()
        val groups = listOf(
            Group(id = 0, name = "a", ownerId = 0, members = emptyList(), transactions = emptyList()),
            Group(id = 1, name = "b", ownerId = 0, members = emptyList(), transactions = emptyList())
        )

        repo.getGroupsResult = Result.success(groups)

        val result = usecase().first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe groups
        repo.getGroupsCallCount shouldBe 1
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")

        repo.getGroupsResult = Result.failure(error)

        val result = usecase().first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.getGroupsCallCount shouldBe 1
    }
})