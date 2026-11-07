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
import model.ShareDetailsRequest
import model.TransactionStatus
import org.milad.expense_share.Amount

class TransactionsRepositoryTest : DescribeSpec({
    describe("getTransactions") {
        context("when API call succeeds") {
            it("should return success with transactions list") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/group123/transactions"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": [
                                    {
                                        "id": 1,
                                        "groupId": 1,
                                        "title": "Hotel Booking",
                                        "amount": 50000,
                                        "description": "2 nights stay",
                                        "createdBy": 1,
                                        "status": "APPROVED",
                                        "createdAt": 1000,
                                        "transactionDate": 1000,
                                        "approvedBy": 1,
                                        "payers": [],
                                        "shareDetails": {
                                            "type": "Equal",
                                            "members": []
                                        }
                                    }
                                ]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.getTransactions("group123").first()

                result shouldBeSuccess { transactions ->
                    transactions.size shouldBe 1
                    transactions[0].title shouldBe "Hotel Booking"
                    transactions[0].amount shouldBe Amount(50000)
                    transactions[0].id shouldBe 1
                    transactions[0].status shouldBe TransactionStatus.APPROVED
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
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.getTransactions("group123").first()

                result.shouldBeFailure()
            }
        }
    }

    describe("createTransaction") {
        context("when API call succeeds") {
            it("should return success with created transaction") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/1/transactions"
                    respond(
                        content = """
                            {
                                "success": true,
                                "data": {
                                    "id": 101,
                                    "groupId": 1,
                                    "title": "New Transaction",
                                    "amount": 25000,
                                    "description": "Test description",
                                    "createdBy": 1,
                                    "status": "PENDING",
                                    "createdAt": 1000,
                                    "transactionDate": 1000,
                                    "approvedBy": null,
                                    "payers": [],
                                    "shareDetails": {
                                        "type": "Equal",
                                        "members": []
                                    }
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.createTransaction(
                    groupId = 1,
                    title = "New Transaction",
                    amount = Amount(25000),
                    description = "Test description",
                    payers = emptyList(),
                    shareDetails = ShareDetailsRequest("Equal", emptyList())
                ).first()

                result shouldBeSuccess { transaction ->
                    transaction.id shouldBe 101
                    transaction.title shouldBe "New Transaction"
                    transaction.amount shouldBe Amount(25000)
                    transaction.description shouldBe "Test description"
                    transaction.status shouldBe TransactionStatus.PENDING
                }
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                val network = createTestNetworkManager { _ ->
                    respond(
                        content = """{"success": false, "data": null}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.createTransaction(
                    groupId = 1,
                    title = "New Transaction",
                    amount = Amount(25000),
                    description = "Test description",
                    payers = emptyList(),
                    shareDetails = ShareDetailsRequest("Equal", emptyList())
                ).first()

                result.shouldBeFailure()
            }
        }
    }

    describe("approveTransaction") {
        context("when API call succeeds") {
            it("should return success with result message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/group123/transactions/tx456/approve"
                    respond(
                        content = """{"success": true, "data": "Transaction approved"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.approveTransaction("group123", "tx456").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Transaction approved"
                }
            }
        }
    }

    describe("rejectTransaction") {
        context("when API call succeeds") {
            it("should return success with result message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/group123/transactions/tx456/reject"
                    respond(
                        content = """{"success": true, "data": "Transaction rejected"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.rejectTransaction("group123", "tx456").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Transaction rejected"
                }
            }
        }
    }

    describe("deleteTransaction") {
        context("when API call succeeds") {
            it("should return success with result message") {
                val network = createTestNetworkManager { request ->
                    request.url.encodedPath.trim('/') shouldBe "groups/group123/transactions/tx456"
                    respond(
                        content = """{"success": true, "data": "Transaction deleted"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val repo = TransactionsRepositoryImpl(network)

                val result = repo.deleteTransaction("group123", "tx456").first()

                result shouldBeSuccess { message ->
                    message shouldBe "Transaction deleted"
                }
            }
        }
    }
})