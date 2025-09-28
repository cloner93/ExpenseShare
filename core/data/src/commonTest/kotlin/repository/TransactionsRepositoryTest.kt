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
import model.CreateTransactionRequest
import model.Transaction
import model.TransactionStatus

class TransactionsRepositoryTest : DescribeSpec({
    val network = mock(of<NetworkManager>())
    val repo = TransactionsRepositoryImpl(network)

    describe("getTransactions") {
        context("when API call succeeds") {
            it("should return success with transactions list") {
                // Arrange
                val groupId = "group123"
                val expectedTransactions = listOf(
                    Transaction(
                        id = 1,
                        groupId = 1,
                        title = "Hotel Booking",
                        amount = 500.0,
                        description = "2 nights stay",
                        createdBy = 1,
                        status = TransactionStatus.APPROVED,
                        approvedBy = 1,
                        createdAt = 0,
                        transactionDate = 0
                    )
                )

                coEvery {
                    network.get<List<Transaction>>("/groups/$groupId/transactions")
                } returns flowOf(Result.success(expectedTransactions))

                // Act
                val result = repo.getTransactions(groupId).first()

                // Assert
                result shouldBeSuccess { transactions ->
                    transactions.size shouldBe 1
                    transactions[0].title shouldBe "Hotel Booking"
                    transactions[0].amount shouldBe 500.0
                    transactions[0].id shouldBe 1
                    transactions[0].status shouldBe TransactionStatus.APPROVED
                }

                coVerify {
                    network.get<List<Transaction>>("/groups/$groupId/transactions")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val exception = RuntimeException("network error")

                coEvery {
                    network.get<List<Transaction>>("/groups/$groupId/transactions")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.getTransactions(groupId).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.get<List<Transaction>>("/groups/$groupId/transactions")
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("createTransaction") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val groupId = "group123"
                val title = "New Transaction"
                val amount = 250.0
                val description = "Test description"
                val expectedTransaction = Transaction(
                    id = 0,
                    title = title,
                    amount = amount,
                    description = description,
                    groupId = 0,
                    createdBy = 0,
                    status = TransactionStatus.PENDING,
                    createdAt = 0,
                    transactionDate = 0,
                    approvedBy = 0
                )
                val request = CreateTransactionRequest(title, amount, description)

                coEvery {
                    network.post<CreateTransactionRequest, Transaction>(
                        "/groups/$groupId/transactions",
                        request
                    )
                } returns flowOf(Result.success(expectedTransaction))

                // Act
                val result = repo.createTransaction(groupId, title, amount, description).first()

                // Assert
                result shouldBeSuccess { transaction ->
                    transaction.title shouldBe title
                    transaction.amount shouldBe amount
                    transaction.description shouldBe description
                    transaction.id shouldBe 0
                }

                coVerify {
                    network.post<CreateTransactionRequest, Transaction>(
                        "/groups/$groupId/transactions",
                        request
                    )
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val title = "New Transaction"
                val amount = 250.0
                val description = "Test description"
                val request = CreateTransactionRequest(title, amount, description)
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<CreateTransactionRequest, Transaction>(
                        "/groups/$groupId/transactions",
                        request
                    )
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.createTransaction(groupId, title, amount, description).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<CreateTransactionRequest, Transaction>(
                        "/groups/$groupId/transactions",
                        request
                    )
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("approveTransaction") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val groupId = "group123"
                val transactionId = "tx456"

                coEvery {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/approve",
                        Unit
                    )
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.approveTransaction(groupId, transactionId).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/approve",
                        Unit
                    )
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val transactionId = "tx456"
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/approve",
                        Unit
                    )
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.approveTransaction(groupId, transactionId).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/approve",
                        Unit
                    )
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("rejectTransaction") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val groupId = "group123"
                val transactionId = "tx456"

                coEvery {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/reject",
                        Unit
                    )
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.rejectTransaction(groupId, transactionId).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/reject",
                        Unit
                    )
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val transactionId = "tx456"
                val exception = RuntimeException("network error")

                coEvery {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/reject",
                        Unit
                    )
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.rejectTransaction(groupId, transactionId).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.post<Unit, Unit>(
                        "/groups/$groupId/transactions/$transactionId/reject",
                        Unit
                    )
                }.wasInvoked(exactly = 1)
            }
        }
    }

    describe("deleteTransaction") {
        context("when API call succeeds") {
            it("should return success") {
                // Arrange
                val groupId = "group123"
                val transactionId = "tx456"

                coEvery {
                    network.delete<Unit>("/groups/$groupId/transactions/$transactionId")
                } returns flowOf(Result.success(Unit))

                // Act
                val result = repo.deleteTransaction(groupId, transactionId).first()

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Unit

                coVerify {
                    network.delete<Unit>("/groups/$groupId/transactions/$transactionId")
                }.wasInvoked(exactly = 1)
            }
        }

        context("when API call fails") {
            it("should return failure with exception") {
                // Arrange
                val groupId = "group123"
                val transactionId = "tx456"
                val exception = RuntimeException("network error")

                coEvery {
                    network.delete<Unit>("/groups/$groupId/transactions/$transactionId")
                } returns flowOf(Result.failure(exception))

                // Act
                val result = repo.deleteTransaction(groupId, transactionId).first()

                // Assert
                result shouldBeFailure { error ->
                    error shouldBe exception
                }

                coVerify {
                    network.delete<Unit>("/groups/$groupId/transactions/$transactionId")
                }.wasInvoked(exactly = 1)
            }
        }
    }
})