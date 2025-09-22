package usecase.transactions

import repository.TransactionsRepository

class GetTransactionsUseCase(private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(groupId: String) = transactionsRepository.getTransactions(groupId)
}