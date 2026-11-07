package usecase.transactions

import fakes.FakeTransactionsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class RejectTransactionUseCaseTest : StringSpec({
    val repo = FakeTransactionsRepository()
    val usecase = RejectTransactionUseCase(repo)

    "should complete successfully when repository succeeds" {
        repo.reset()
        val groupId = "123"
        val transactionId = "1"

        repo.rejectTransactionResult = Result.success("Success")

        val result = usecase(groupId, transactionId).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.rejectTransactionCallCount shouldBe 1
        repo.lastRejectTransactionArgs shouldBe Pair(groupId, transactionId)
    }

    "should return failure when repository fails" {
        repo.reset()
        val groupId = "123"
        val transactionId = "1"
        val error = RuntimeException("network error")

        repo.rejectTransactionResult = Result.failure(error)

        val result = usecase(groupId, transactionId).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.rejectTransactionCallCount shouldBe 1
        repo.lastRejectTransactionArgs shouldBe Pair(groupId, transactionId)
    }
})

