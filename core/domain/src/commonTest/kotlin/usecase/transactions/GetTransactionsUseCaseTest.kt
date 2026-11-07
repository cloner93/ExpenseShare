package usecase.transactions

import fakes.FakeTransactionsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.ShareDetailsRequest
import model.Transaction
import model.TransactionStatus
import org.milad.expense_share.Amount

class GetTransactionsUseCaseTest : StringSpec({
    val repo = FakeTransactionsRepository()
    val usecase = GetTransactionsUseCase(repo)

    "should return transactions when repository succeeds" {
        repo.reset()
        val groupId = "1"
        val transactions = listOf(
            Transaction(
                id = 1,
                groupId = 100,
                title = "Dinner at Restaurant",
                amount = Amount(250),
                description = "Team dinner with colleagues",
                createdBy = 42,
                status = TransactionStatus.PENDING,
                createdAt = 0L,
                transactionDate = 0L,
                approvedBy = null,
                payers = emptyList(),
                shareDetails = ShareDetailsRequest("Equal", emptyList())
            )
        )

        repo.getTransactionsResult = Result.success(transactions)

        val result = usecase(groupId).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe transactions
        repo.getTransactionsCallCount shouldBe 1
        repo.lastGetTransactionsGroupId shouldBe groupId
    }

    "should return failure when repository fails" {
        repo.reset()
        val groupId = "123"
        val error = RuntimeException("network error")

        repo.getTransactionsResult = Result.failure(error)

        val result = usecase(groupId).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.getTransactionsCallCount shouldBe 1
        repo.lastGetTransactionsGroupId shouldBe groupId
    }
})

