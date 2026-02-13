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
import model.User

class FriendsRepositoryTest : DescribeSpec({
    val network = mock(of<NetworkManager>())
    val repo = FriendsRepositoryImpl(network)

    describe("getAllFriends") {
        context("when API call succeeds") {
            it("should return success with friends list") {
                // Arrange
                val expectedUsers = listOf(User(1, "Milad", "phone"))

                coEvery {
                    network.get<List<User>>("/friends")
                } returns flowOf(Result.success(expectedUsers))

                // Act
                val result = repo.getAllFriends().first()

                // Assert
                result shouldBeSuccess { user ->
                    user.size shouldBe 1
                    user[0].username shouldBe "Milad"
                }

                coVerify {
                    network.get<List<User>>("/friends")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val exception = RuntimeException("network error")

                coEvery {
                    network.get<List<User>>("/friends")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.getAllFriends().first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.get<List<User>>("/friends")
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("getFriendRequests") {
        context("when API call succeeds") {
            it("should return success with friend requests list") {
                // Arrange
                val expectedUsers = listOf(User(1, "Milad", "phone"))

                coEvery {
                    network.get<List<User>>("/friends/requests")
                } returns flowOf(Result.success(expectedUsers))

                // Act
                val result = repo.getFriendRequests().first()

                // Assert
                result shouldBeSuccess { user ->
                    user.size shouldBe 1
                    user[0].username shouldBe "Milad"
                }

                coVerify {
                    network.get<List<User>>("/friends/requests")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val exception = RuntimeException("network error")

                coEvery {
                    network.get<List<User>>("/friends/requests")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.getFriendRequests().first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.get<List<User>>("/friends/requests")
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("sendFriendRequest") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val phone = "09123456789"
                coEvery {
                    network.post<String, Unit>("/friends/request", phone)
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.sendFriendRequest(phone).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.post<String, Unit>("/friends/request", phone)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val phone = "09123456789"
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<String, Unit>("/friends/request", phone)
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.sendFriendRequest(phone).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<String, Unit>("/friends/request", phone)
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("acceptFriendRequest") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val phone = "09123456789"
                coEvery {
                    network.post<String, Unit>("/friends/accept", phone)
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.acceptFriendRequest(phone).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.post<String, Unit>("/friends/accept", phone)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val phone = "09123456789"
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<String, Unit>("/friends/accept", phone)
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.acceptFriendRequest(phone).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<String, Unit>("/friends/accept", phone)
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("rejectFriendRequest") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val phone = "09123456789"
                coEvery {
                    network.post<String, Unit>("/friends/reject", phone)
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.rejectFriendRequest(phone).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.post<String, Unit>("/friends/reject", phone)
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val phone = "09123456789"
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<String, Unit>("/friends/reject", phone)
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.rejectFriendRequest(phone).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<String, Unit>("/friends/reject", phone)
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("removeFriend") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val phone = "09123456778"
                coEvery {
                    network.delete<Unit>("/friends/$phone")
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.removeFriend(phone).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.delete<Unit>("/friends/$phone")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val phone = "09123456778"
                val exception = RuntimeException("network error")

                coEvery {
                    network.delete<Unit>("/friends/$phone")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.removeFriend(phone).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.delete<Unit>("/friends/$phone")
                }.wasInvoked(exactly = 1)
            }
        }
    }
})