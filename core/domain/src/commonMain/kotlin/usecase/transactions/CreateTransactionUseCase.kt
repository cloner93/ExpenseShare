package usecase.transactions

import model.PayerDto
import model.SplitDetailsDto
import repository.TransactionsRepository

class CreateTransactionUseCase(private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(
        groupId: Int,
        title: String,
        amount: Double,
        description: String?,
        payers: List<PayerDto>?,
        splitDetails: SplitDetailsDto?,
    ) = transactionsRepository.createTransaction(groupId, title, amount, description,payers ,splitDetails)
}