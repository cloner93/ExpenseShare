package org.milad.expense_share.domain.service

import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest

class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val settlementService: SettlementService,
) {

    fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Amount,
        description: String,
        payers: List<PayerRequest>,
        shareDetails: ShareDetailsRequest,
    ): Result<Transaction> {
        val transaction = transactionRepository.createTransaction(
            groupId, userId, title, amount, description, payers, shareDetails
        )
        return transaction?.let {
            settlementService.recalculate(groupId, userId)
            Result.success(it)
        } ?: Result.failure(IllegalStateException("Group not found or access denied"))
    }

    fun getTransactions(userId: Int, groupId: Int): List<Transaction> =
        transactionRepository.getTransactions(userId, groupId)

    fun approve(transactionId: Int, userId: Int): Result<String> {
        val approved = transactionRepository.approveTransaction(transactionId, userId)
        if (!approved) return Result.failure(IllegalAccessException("Only group owner can approve"))

        val groupId = transactionRepository.getGroupIdByTransaction(transactionId)
        groupId?.let { settlementService.recalculate(it, userId) }

        return Result.success("Transaction approved successfully")
    }

    fun reject(transactionId: Int, userId: Int): Result<String> {
        val rejected = transactionRepository.rejectTransaction(transactionId, userId)
        if (!rejected) return Result.failure(IllegalAccessException("Only group owner can reject"))

        val groupId = transactionRepository.getGroupIdByTransaction(transactionId)
        groupId?.let { settlementService.recalculate(it, userId) }

        return Result.success("Transaction rejected successfully")
    }

    fun delete(transactionId: Int, userId: Int): Result<String> {
        val groupId = transactionRepository.getGroupIdByTransaction(transactionId)

        val deleted = transactionRepository.deleteTransaction(transactionId, userId)
        if (!deleted) return Result.failure(IllegalAccessException("Only group owner can delete"))

        groupId?.let { settlementService.recalculate(it, userId) }

        return Result.success("Transaction deleted successfully")
    }
}