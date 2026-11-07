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

class GroupsRepositoryTest : DescribeSpec({
    describe("getGroups") {
        context("when API call succeeds") {
            it("should return success with groups list") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": [
                                    {
                                        "id": 1,
                                        "name": "Test Group",
                                        "ownerId": 1,
                                        "members": [],
                                        "transactions": []
                                    }
                                ]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = GroupsRepositoryImpl(network)

                val result = repo.getGroups().first()

                result shouldBeSuccess { groups ->
                    groups.size shouldBe 1
                    groups[0].name shouldBe "Test Group"
                    groups[0].id shouldBe 1
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
                val repo = GroupsRepositoryImpl(network)

                val result = repo.getGroups().first()

                result.shouldBeFailure()
            }
        }
    }

    describe("createGroup") {
        context("when API call succeeds") {
            it("should return success with created group") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/create"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": {
                                    "id": 10,
                                    "name": "New Group",
                                    "ownerId": 1,
                                    "members": [],
                                    "transactions": []
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = GroupsRepositoryImpl(network)

                val result = repo.createGroup("New Group", listOf(1, 2)).first()

                result shouldBeSuccess { group ->
                    group.name shouldBe "New Group"
                    group.id shouldBe 10
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
                val repo = GroupsRepositoryImpl(network)

                val result = repo.createGroup("New Group", listOf(1, 2)).first()

                result.shouldBeFailure()
            }
        }
    }

    describe("updateGroupMembers") {
        context("when API call succeeds") {
            it("should return success with result message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/10/updateMembers"
                    respond(
                        content = """{"success": true, "data": "Members updated successfully"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = GroupsRepositoryImpl(network)

                val result = repo.updateGroupMembers("10", listOf(1, 2, 3)).first()

                result shouldBeSuccess { message ->
                    message shouldBe "Members updated successfully"
                }
            }
        }
    }

    describe("deleteGroup") {
        context("when API call succeeds") {
            it("should return success with result message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/10"
                    respond(
                        content = """{"success": true, "data": "Group deleted successfully"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = GroupsRepositoryImpl(network)

                val result = repo.deleteGroup("10").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Group deleted successfully"
                }
            }
        }
    }
})