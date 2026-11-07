package usecase.groups

import fakes.FakeGroupsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.Group

class CreateGroupUseCaseTest : StringSpec({
    val repo = FakeGroupsRepository()
    val usecase = CreateGroupUseCase(repo)

    val groupName = "group name"
    val members = listOf(1, 2)

    "should return created group when repository succeeds" {
        repo.reset()
        val group = Group(id = 0, name = groupName, ownerId = 0, members = emptyList(), transactions = emptyList())
        repo.createGroupResult = Result.success(group)

        val result = usecase(groupName, members).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe group
        repo.createGroupCallCount shouldBe 1
        repo.lastCreateGroupName shouldBe groupName
        repo.lastCreateGroupMemberIds shouldBe members
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.createGroupResult = Result.failure(error)

        val result = usecase(groupName, members).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.createGroupCallCount shouldBe 1
        repo.lastCreateGroupName shouldBe groupName
        repo.lastCreateGroupMemberIds shouldBe members
    }
})


