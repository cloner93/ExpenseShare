package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.domain.repository.TransactionRepository

class TransactionService(private val transactionRepository: TransactionRepository) {

    fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Double,
        description: String
    ): Result<Transaction> {
        val transaction =
            transactionRepository.createTransaction(groupId, userId, title, amount, description)
        return transaction?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Group not found or access denied"))
    }

    fun getTransactions(userId: Int, groupId: Int): List<Transaction> =
        transactionRepository.getTransactions(userId, groupId)

    fun approve(transactionId: Int, userId: Int): Result<String> =
        if (transactionRepository.approveTransaction(transactionId, userId))
            Result.success("Transaction approved successfully")
        else Result.failure(IllegalAccessException("Only group owner can approve"))

    fun reject(transactionId: Int, userId: Int): Result<String> =
        if (transactionRepository.rejectTransaction(transactionId, userId))
            Result.success("Transaction rejected successfully")
        else Result.failure(IllegalAccessException("Only group owner can reject"))

    fun delete(transactionId: Int, userId: Int): Result<String> =
        if (transactionRepository.deleteTransaction(transactionId, userId))
            Result.success("Transaction deleted successfully")
        else Result.failure(IllegalAccessException("Only group owner can delete"))
}
