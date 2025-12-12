package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.CreateTransactionRequest
import model.PayerDto
import model.ShareDetailsRequest
import model.Transaction
import org.milad.expense_share.Amount


class TransactionsRepositoryImpl(private val networkManager: NetworkManager) :
    TransactionsRepository {
    override suspend fun getTransactions(groupId: String): Flow<Result<List<Transaction>>> {
        return networkManager.get<List<Transaction>>("/groups/$groupId/transactions")
    }

    override suspend fun createTransaction(
        groupId: Int,
        title: String,
        amount: Amount,
        description: String?,
        payers: List<PayerDto>?,
        shareDetails: ShareDetailsRequest?
    ): Flow<Result<Transaction>> {
        return networkManager.post(
            "/groups/$groupId/transactions",
            body = CreateTransactionRequest(
                title,
                amount,
                description,
                payers = payers,
                shareDetails = shareDetails
            )
        )
    }

    override suspend fun approveTransaction(
        groupId: String, transactionId: String
    ): Flow<Result<String>> {
        return networkManager.post("/groups/$groupId/transactions/$transactionId/approve", Unit)
    }

    override suspend fun rejectTransaction(
        groupId: String, transactionId: String
    ): Flow<Result<String>> {
        return networkManager.post("/groups/$groupId/transactions/$transactionId/reject", Unit)
    }

    override suspend fun deleteTransaction(
        groupId: String,
        transactionId: String
    ): Flow<Result<String>> {
        return networkManager.delete("/groups/$groupId/transactions/$transactionId")
    }
}