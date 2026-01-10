package org.milad.expense_share.domain.repository

import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest

interface TransactionRepository {
    fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Amount,
        description: String,
        payers: List<PayerRequest>,
        shareDetails: ShareDetailsRequest,
    ): Transaction?

    fun getTransactions(userId: Int, groupId: Int): List<Transaction>
    fun approveTransaction(transactionId: Int, managerId: Int): Boolean
    fun rejectTransaction(transactionId: Int, managerId: Int): Boolean
    fun deleteTransaction(transactionId: Int, managerId: Int): Boolean
}
