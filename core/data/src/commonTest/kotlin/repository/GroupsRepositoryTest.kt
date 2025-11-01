package repository

import NetworkManager
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.CreateGroupRequest
import model.Group

class GroupsRepositoryTest : DescribeSpec({
    val network = mock(of<NetworkManager>())
    val repo = GroupsRepositoryImpl(network)

    describe("getGroups") {
        context("when API call succeeds") {
            it("should return success with groups list") {
                // Arrange
                val expectedGroups = listOf(Group(
                    id = 1, name = "Test Group", ownerId = 1,
                    members = listOf(),
                    transactions = listOf()
                ))

                coEvery {
                    network.get<List<Group>>("/groups")
                } returns flowOf(Result.success(expectedGroups))

                // Act
                val result = repo.getGroups().first()

                // Assert
                result shouldBeSuccess { groups ->
                    groups.size shouldBe 1
                    groups[0].name shouldBe "Test Group"
                    groups[0].id shouldBe 1
                }

                coVerify {
                    network.get<List<Group>>("/groups")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val exception = RuntimeException("network error")

                coEvery {
                    network.get<List<Group>>("/groups")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.getGroups().first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.get<List<Group>>("/groups")
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("createGroup") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val name = "New Group"
                val expectedGroup = Group(id = 1, name = name, ownerId = 1)
                val memberIds = listOf("user1", "user2")
                val request = CreateGroupRequest(name, memberIds)

                coEvery {
                    network.post<CreateGroupRequest, Group>("/groups", request)
                } returns flowOf(Result.success(expectedGroup))

                // Act
                val result = repo.createGroup(name, memberIds).first()

                // Assert
                result shouldBeSuccess { group ->
                    group.name shouldBe name
                    group.id shouldBe 1
                }

                coVerify {
                    network.post<CreateGroupRequest, Group>("/groups", request)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val name = "New Group"
                val memberIds = listOf("user1", "user2")
                val request = CreateGroupRequest(name, memberIds)
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<CreateGroupRequest, Group>("/groups", request)
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.createGroup(name, memberIds).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<CreateGroupRequest, Group>("/groups", request)
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("updateGroupMembers") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val groupId = "group123"
                val memberIds = listOf("user1", "user2", "user3")

                coEvery {
                    network.put<List<String>, Unit>("/groups/$groupId/updateMembers", memberIds)
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.updateGroupMembers(groupId, memberIds).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.put<List<String>, Unit>("/groups/$groupId/updateMembers", memberIds)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val memberIds = listOf("user1", "user2", "user3")
                val exception = RuntimeException("network error")

                coEvery {
                    network.put<List<String>, Unit>("/groups/$groupId/updateMembers", memberIds)
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.updateGroupMembers(groupId, memberIds).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.put<List<String>, Unit>("/groups/$groupId/updateMembers", memberIds)
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("deleteGroup") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val groupId = "group123"

                coEvery {
                    network.delete<Unit>("/groups/$groupId")
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.deleteGroup(groupId).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.delete<Unit>("/groups/$groupId")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val exception = RuntimeException("network error")

                coEvery {
                    network.delete<Unit>("/groups/$groupId")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.deleteGroup(groupId).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.delete<Unit>("/groups/$groupId")
                }.wasInvoked(exactly = 1)
            }
        }
    }
})