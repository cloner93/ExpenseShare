package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.Transaction

interface TransactionRepository {
    fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Double,
        description: String
    ): Transaction?

    fun getTransactions(userId: Int, groupId: Int): List<Transaction>
    fun approveTransaction(transactionId: Int, managerId: Int): Boolean
    fun rejectTransaction(transactionId: Int, managerId: Int): Boolean
    fun deleteTransaction(transactionId: Int, managerId: Int): Boolean
}
