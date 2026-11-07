package repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import model.FriendRelationStatus

class FriendsRepositoryTest : DescribeSpec({
    describe("getAllFriends") {
        context("when API call succeeds") {
            it("should return success with friends list") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "friends"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": [
                                    {
                                        "user": {
                                            "id": 1,
                                            "username": "Milad",
                                            "phone": "09123456789"
                                        },
                                        "status": "ACCEPTED",
                                        "requestedBy": 1,
                                        "createdAt": 1000,
                                        "updatedAt": 1000
                                    }
                                ]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.getAllFriends().first()

                result shouldBeSuccess { friends ->
                    friends.size shouldBe 1
                    friends[0].user.username shouldBe "Milad"
                    friends[0].status shouldBe FriendRelationStatus.ACCEPTED
                }
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                val network = createTestNetworkManager { _ ->
                    respond(
                        content = """{"success": false, "data": null}""",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.getAllFriends().first()

                result.shouldBeFailure()
            }
        }
    }

    describe("sendFriendRequest") {
        context("when API call succeeds") {
            it("should return success with message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "friends/request"
                    respond(
                        content = """{"success": true, "data": "Friend request sent"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.sendFriendRequest("09123456789").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Friend request sent"
                }
            }
        }

        context("when API call fails") {
            it("should return failure") {
                val network = createTestNetworkManager { _ ->
                    respond(
                        content = """{"success": false, "data": null}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.sendFriendRequest("09123456789").first()

                result.shouldBeFailure()
            }
        }
    }

    describe("acceptFriendRequest") {
        context("when API call succeeds") {
            it("should return success with message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "friends/accept"
                    respond(
                        content = """{"success": true, "data": "Friend request accepted"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.acceptFriendRequest("09123456789").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Friend request accepted"
                }
            }
        }
    }

    describe("rejectFriendRequest") {
        context("when API call succeeds") {
            it("should return success with message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "friends/reject"
                    respond(
                        content = """{"success": true, "data": "Friend request rejected"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.rejectFriendRequest("09123456789").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Friend request rejected"
                }
            }
        }
    }

    describe("removeFriend") {
        context("when API call succeeds") {
            it("should return success with message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "friends/remove"
                    respond(
                        content = """{"success": true, "data": "Friend removed"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = FriendsRepositoryImpl(network)

                val result = repo.removeFriend("09123456789").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Friend removed"
                }
            }
        }
    }
})