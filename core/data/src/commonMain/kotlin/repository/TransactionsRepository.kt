package repository

import kotlinx.coroutines.flow.Flow
import model.Transaction
import utils.Result

interface TransactionsRepository {
    suspend fun getTransactions(groupId: String): Flow<Result<List<Transaction>>>
    suspend fun createTransaction(groupId: String, title: String, amount: Double, description: String?):Flow< Result<Transaction>>
    suspend fun approveTransaction(transactionId: String):Flow< Result<Unit>>
    suspend fun rejectTransaction(transactionId: String):Flow< Result<Unit>>
    suspend fun deleteTransaction(transactionId: String):Flow< Result<Unit>>
}