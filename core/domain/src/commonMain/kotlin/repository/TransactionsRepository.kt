package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.PayerDto
import model.ShareDetailsRequest
import model.Transaction
import org.milad.expense_share.Amount

@Mockable
interface TransactionsRepository {
    suspend fun getTransactions(groupId: String): Flow<Result<List<Transaction>>>
    suspend fun createTransaction(
        groupId: Int,
        title: String,
        amount: Amount,
        description: String?,
        payers: List<PayerDto>?,
        shareDetails: ShareDetailsRequest?,
    ): Flow<Result<Transaction>>

    suspend fun approveTransaction(groupId: String, transactionId: String): Flow<Result<String>>
    suspend fun rejectTransaction(groupId: String, transactionId: String): Flow<Result<String>>
    suspend fun deleteTransaction(groupId: String, transactionId: String): Flow<Result<String>>
}