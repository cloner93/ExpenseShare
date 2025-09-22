package usecase.transactions

import repository.TransactionsRepository

class DeleteTransactionUseCase (private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(groupId: String, transactionId: String) =
        transactionsRepository.deleteTransaction(groupId, transactionId)
}