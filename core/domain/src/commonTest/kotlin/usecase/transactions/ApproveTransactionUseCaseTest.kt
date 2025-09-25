package usecase.transactions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import repository.TransactionsRepository

class ApproveTransactionUseCaseTest : StringSpec({
    val repo = mock(of<TransactionsRepository>())
    val usecase = ApproveTransactionUseCase(repo)

    "should complete successfully when repository succeeds" {
        val groupId = "123"
        val transactionId = "1"

        coEvery { repo.approveTransaction(groupId, transactionId) }
            .returns(flowOf(Result.success(Unit)))

        val result = usecase(groupId, transactionId).first()

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe Unit
        coVerify { repo.approveTransaction(groupId, transactionId) }
            .wasInvoked(exactly = 1)
    }

    "should return failure when repository fails" {
        val groupId = "123"
        val transactionId = "1"
        val error = RuntimeException("network error")

        coEvery { repo.approveTransaction(groupId, transactionId) }
            .returns(flowOf(Result.failure(error)))

        val result = usecase(groupId, transactionId).first()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe error
        coVerify { repo.approveTransaction(groupId, transactionId) }
            .wasInvoked(exactly = 1)
    }
})
