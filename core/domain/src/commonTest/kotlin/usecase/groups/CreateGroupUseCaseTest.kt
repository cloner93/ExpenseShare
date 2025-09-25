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

class CreateGroupUseCaseTest : StringSpec({
    val repo = mock(of<GroupsRepository>())
    val usecase = CreateGroupUseCase(repo)

    val groupName = "group name"
    val members = listOf("member1", "member2")

    "should return created group when repository succeeds" {
        // Arrange
        val group = Group(id = 0, name = groupName, ownerId = 0)

        coEvery { repo.createGroup(groupName, members) }
            .returns(flowOf(Result.success(group)))

        // Act
        val result = usecase(groupName, members).first()

        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe group
        coVerify { repo.createGroup(groupName, members) }
            .wasInvoked(exactly = 1)
    }
    "should return failure when repository fails" {
        // Arrange
        val error = RuntimeException("network error")

        coEvery { repo.createGroup(groupName, members) }
            .returns(flowOf(Result.failure(error)))

        // Act
        val result = usecase(groupName, members).first()

        // Assert
        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        coVerify { repo.createGroup(groupName, members) }
            .wasInvoked(exactly = 1)
    }
})

