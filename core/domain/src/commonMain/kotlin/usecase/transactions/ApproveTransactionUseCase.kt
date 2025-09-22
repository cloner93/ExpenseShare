package usecase.transactions

import repository.TransactionsRepository

class ApproveTransactionUseCase(private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(groupId: String, transactionId: String) =
        transactionsRepository.approveTransaction(groupId, transactionId)
}