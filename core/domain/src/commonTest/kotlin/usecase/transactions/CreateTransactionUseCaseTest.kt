package usecase.transactions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import model.Transaction
import model.TransactionStatus
import repository.TransactionsRepository

class CreateTransactionUseCaseTest : StringSpec({
    val repo = mock(of<TransactionsRepository>())
    val usecase = CreateTransactionUseCase(repo)

    "should return created transaction when repository succeeds" {
        val groupId = "123"
        val transaction = Transaction(
            id = 1,
            groupId = 123,
            title = "Dinner at Restaurant",
            amount = 250.0,
            description = "Team dinner with colleagues",
            createdBy = 42,
            status = TransactionStatus.PENDING,
            createdAt = 0,
            transactionDate = 0,
            approvedBy = null
        )

        coEvery { repo.createTransaction(groupId, "Dinner at Restaurant", 250.0, "Team dinner with colleagues") }
            .returns(flowOf(Result.success(transaction)))

        val result = usecase(groupId, "Dinner at Restaurant", 250.0, "Team dinner with colleagues").first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe transaction
        coVerify { repo.createTransaction(groupId, "Dinner at Restaurant", 250.0, "Team dinner with colleagues") }
            .wasInvoked(exactly = 1)
    }

    "should return failure when repository fails" {
        val groupId = "123"
        val error = RuntimeException("network error")

        coEvery { repo.createTransaction(groupId, "Dinner at Restaurant", 250.0, "Team dinner with colleagues") }
            .returns(flowOf(Result.failure(error)))

        val result = usecase(groupId, "Dinner at Restaurant", 250.0, "Team dinner with colleagues").first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        coVerify { repo.createTransaction(groupId, "Dinner at Restaurant", 250.0, "Team dinner with colleagues") }
            .wasInvoked(exactly = 1)
    }
})
