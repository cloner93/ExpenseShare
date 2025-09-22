package usecase.transactions

import repository.TransactionsRepository

class RejectTransactionUseCase (private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(groupId: String, transactionId: String) =
        transactionsRepository.rejectTransaction(groupId, transactionId)
}