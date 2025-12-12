package usecase.transactions

import model.PayerDto
import model.ShareDetailsRequest
import org.milad.expense_share.Amount
import repository.TransactionsRepository

class CreateTransactionUseCase(private val transactionsRepository: TransactionsRepository) {
    suspend operator fun invoke(
        groupId: Int,
        title: String,
        amount: Amount,
        description: String?,
        payers: List<PayerDto>?,
        shareDetails: ShareDetailsRequest?,
    ) = transactionsRepository.createTransaction(
        groupId,
        title,
        amount,
        description,
        payers,
        shareDetails
    )
}