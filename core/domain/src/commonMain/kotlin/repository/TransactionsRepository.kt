package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.Transaction

@Mockable
interface TransactionsRepository {
    suspend fun getTransactions(groupId: String): Flow<Result<List<Transaction>>>
    suspend fun createTransaction(groupId: Int, title: String, amount: Double, description: String?):Flow< Result<Transaction>>
    suspend fun approveTransaction(groupId: String, transactionId: String): Flow<Result<Unit>>
    suspend fun rejectTransaction(groupId: String, transactionId: String): Flow<Result<Unit>>
    suspend fun deleteTransaction(groupId: String, transactionId: String): Flow<Result<Unit>>
}