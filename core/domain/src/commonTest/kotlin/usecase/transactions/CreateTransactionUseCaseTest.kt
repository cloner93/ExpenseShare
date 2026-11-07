package usecase.transactions

import fakes.FakeTransactionsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import model.PayerDto
import model.ShareDetailsRequest
import model.Transaction
import model.TransactionStatus
import org.milad.expense_share.Amount

class CreateTransactionUseCaseTest : StringSpec({
    val repo = FakeTransactionsRepository()
    val usecase = CreateTransactionUseCase(repo)

    "should return created transaction when repository succeeds" {
        repo.reset()
        val groupId = 123
        val amount = Amount(250)
        val payers = emptyList<PayerDto>()
        val shareDetails = ShareDetailsRequest("Equal", emptyList())
        val transaction = Transaction(
            id = 1,
            groupId = 123,
            title = "Dinner at Restaurant",
            amount = amount,
            description = "Team dinner with colleagues",
            createdBy = 42,
            status = TransactionStatus.PENDING,
            createdAt = 0L,
            transactionDate = 0L,
            approvedBy = null,
            payers = payers,
            shareDetails = shareDetails
        )

        repo.createTransactionResult = Result.success(transaction)

        val result = usecase(groupId, "Dinner at Restaurant", amount, "Team dinner with colleagues", payers, shareDetails).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe transaction
        repo.createTransactionCallCount shouldBe 1
        repo.lastCreateTransactionArgs?.groupId shouldBe groupId
        repo.lastCreateTransactionArgs?.title shouldBe "Dinner at Restaurant"
        repo.lastCreateTransactionArgs?.amount shouldBe amount
    }

    "should return failure when repository fails" {
        repo.reset()
        val groupId = 123
        val amount = Amount(250)
        val payers = emptyList<PayerDto>()
        val shareDetails = ShareDetailsRequest("Equal", emptyList())
        val error = RuntimeException("network error")

        repo.createTransactionResult = Result.failure(error)

        val result = usecase(groupId, "Dinner at Restaurant", amount, "Team dinner with colleagues", payers, shareDetails).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.createTransactionCallCount shouldBe 1
        repo.lastCreateTransactionArgs?.groupId shouldBe groupId
    }
})

