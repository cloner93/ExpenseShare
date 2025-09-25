package usecase.groups

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import repository.GroupsRepository

class UpdateGroupMembersUseCaseTest : StringSpec({
    val repo = mock(of<GroupsRepository>())
    val usecase = UpdateGroupMembersUseCase(repo)
    val groupsId = "0"
    val members = listOf("1", "2")

    "should complete successfully when repository succeeds" {
        // Arrange

        coEvery {
            repo.updateGroupMembers(
                groupId = groupsId,
                memberIds = members
            )
        }.returns(flowOf(Result.success(Unit)))

        // Act
        val result = usecase(
            groupId = groupsId,
            memberIds = members
        ).first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe Unit
        coVerify {
            repo.updateGroupMembers(
                groupId = groupsId,
                memberIds = members
            )
        }.wasInvoked(exactly = 1)
    }
    "should return failure when repository fails" {
        // Arrange
        val error = RuntimeException("network error")

        coEvery {
            repo.updateGroupMembers(
                groupId = groupsId,
                memberIds = members
            )
        }.returns(flowOf(Result.failure(error)))

        // Act
        val result = usecase(
            groupId = groupsId,
            memberIds = members
        ).first()

        // Assert
        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        coVerify {
            repo.updateGroupMembers(
                groupId = groupsId,
                memberIds = members
            )
        }
            .wasInvoked(exactly = 1)
    }
})