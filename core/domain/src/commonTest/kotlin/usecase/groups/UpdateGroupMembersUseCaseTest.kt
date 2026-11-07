package usecase.groups

import fakes.FakeGroupsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class UpdateGroupMembersUseCaseTest : StringSpec({
    val repo = FakeGroupsRepository()
    val usecase = UpdateGroupMembersUseCase(repo)
    val groupsId = "0"
    val members = listOf(1, 2)

    "should complete successfully when repository succeeds" {
        repo.reset()
        repo.updateGroupMembersResult = Result.success("Success")

        val result = usecase(
            groupId = groupsId,
            memberIds = members
        ).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.updateGroupMembersCallCount shouldBe 1
        repo.lastUpdateGroupId shouldBe groupsId
        repo.lastUpdateGroupMemberIds shouldBe members
    }

    "should return failure when repository fails" {
        repo.reset()
        val error = RuntimeException("network error")
        repo.updateGroupMembersResult = Result.failure(error)

        val result = usecase(
            groupId = groupsId,
            memberIds = members
        ).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.updateGroupMembersCallCount shouldBe 1
        repo.lastUpdateGroupId shouldBe groupsId
        repo.lastUpdateGroupMemberIds shouldBe members
    }
})