package usecase.groups

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.Group
import repository.GroupsRepository

class GetGroupsUseCaseTest : StringSpec({
    val repo = mock(of<GroupsRepository>())
    val usecase = GetGroupsUseCase(repo)

    "should return groups when repository succeeds" {
        // Arrange
        val groups = listOf<Group>(
            Group(id = 0, name = "a", ownerId = 0),
            Group(id = 1, name = "b", ownerId = 0)
        )

        coEvery { repo.getGroups() }
            .returns(flowOf(Result.success(groups)))

        // Act
        val result = usecase().first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe groups
        coVerify { repo.getGroups() }
            .wasInvoked(exactly = 1)
    }
    "should return failure when repository fails" {
        // Arrange
        val error = RuntimeException("network error")

        coEvery { repo.getGroups() }
            .returns(flowOf(Result.failure(error)))

        // Act
        val result = usecase().first()

        // Assert
        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        coVerify { repo.getGroups() }
            .wasInvoked(exactly = 1)
    }
})