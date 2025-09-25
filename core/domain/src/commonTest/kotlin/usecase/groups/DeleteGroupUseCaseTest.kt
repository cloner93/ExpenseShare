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

class DeleteGroupUseCaseTest : StringSpec({
    val repo = mock(of<GroupsRepository>())
    val usecase = DeleteGroupUseCase(repo)

    "should complete successfully when repository succeeds" {
        // Arrange
        val groupId = "0"

        coEvery { repo.deleteGroup(groupId) }
            .returns(flowOf(Result.success(Unit)))

        // Act
        val result = usecase(groupId).first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe Unit
        coVerify { repo.deleteGroup(groupId) }
            .wasInvoked(exactly = 1)
    }
    "should return failure when repository fails" {
        // Arrange
        val groupId = "0"
        val error = RuntimeException("network error")

        coEvery { repo.deleteGroup(groupId) }
            .returns(flowOf(Result.failure(error)))

        // Act
        val result = usecase(groupId).first()

        // Assert
        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        coVerify { repo.deleteGroup(groupId) }
            .wasInvoked(exactly = 1)
    }
})