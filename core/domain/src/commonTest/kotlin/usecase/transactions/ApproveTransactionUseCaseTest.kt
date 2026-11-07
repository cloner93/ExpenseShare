package usecase.transactions

import fakes.FakeTransactionsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class ApproveTransactionUseCaseTest : StringSpec({
    val repo = FakeTransactionsRepository()
    val usecase = ApproveTransactionUseCase(repo)

    "should complete successfully when repository succeeds" {
        repo.reset()
        val groupId = "123"
        val transactionId = "1"

        repo.approveTransactionResult = Result.success("Success")

        val result = usecase(groupId, transactionId).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe "Success"
        repo.approveTransactionCallCount shouldBe 1
        repo.lastApproveTransactionArgs shouldBe Pair(groupId, transactionId)
    }

    "should return failure when repository fails" {
        repo.reset()
        val groupId = "123"
        val transactionId = "1"
        val error = RuntimeException("network error")

        repo.approveTransactionResult = Result.failure(error)

        val result = usecase(groupId, transactionId).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        repo.approveTransactionCallCount shouldBe 1
        repo.lastApproveTransactionArgs shouldBe Pair(groupId, transactionId)
    }
})

