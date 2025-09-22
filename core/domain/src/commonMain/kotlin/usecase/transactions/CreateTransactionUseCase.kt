package usecase.transactions

import repository.TransactionsRepository

class CreateTransactionUseCase(private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(
        groupId: String,
        title: String,
        amount: Double,
        description: String?
    ) = transactionsRepository.createTransaction(groupId, title, amount, description)
}