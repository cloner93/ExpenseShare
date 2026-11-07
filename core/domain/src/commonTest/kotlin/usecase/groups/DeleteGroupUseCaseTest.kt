package usecase.groups

import fakes.FakeGroupsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class DeleteGroupUseCaseTest : StringSpec({
    val repo = FakeGroupsRepository()
    val usecase = DeleteGroupUseCase(repo)

    "should complete successfully when repository succeeds" {
        repo.reset()
        val groupId = "0"
        repo.deleteGroupResult = Result.success("Success")

        val result = usecase(groupId).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.deleteGroupCallCount shouldBe 1
        repo.lastDeleteGroupId shouldBe groupId
    }

    "should return failure when repository fails" {
        repo.reset()
        val groupId = "0"
        val error = RuntimeException("network error")
        repo.deleteGroupResult = Result.failure(error)

        val result = usecase(groupId).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.deleteGroupCallCount shouldBe 1
        repo.lastDeleteGroupId shouldBe groupId
    }
})