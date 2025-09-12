package org.milad.expense_share.database

import org.milad.expense_share.database.FakeDatabase.groups
import org.milad.expense_share.database.FakeDatabase.transactions
import org.milad.expense_share.database.models.Transaction
import org.milad.expense_share.database.models.TransactionStatus

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

class InMemoryTransactionRepository : TransactionRepository {
    override fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Double,
        description: String
    ): Transaction? {
        val group = groups.find { it.id == groupId } ?: return null

        val status = if (group.ownerId == userId) {
            TransactionStatus.APPROVED
        } else {
            TransactionStatus.PENDING
        }

        val tx = Transaction(
            id = transactions.size + 1,
            groupId = groupId,
            title = title,
            amount = amount,
            description = description,
            createdBy = userId,
            status = status,
            approvedBy = if (status == TransactionStatus.APPROVED) userId else null
        )
        transactions.add(tx)
        return tx
    }

    override fun getTransactions(userId: Int, groupId: Int): List<Transaction> {
        val group = groups.find { it.id == groupId } ?: return emptyList()

        return if (group.ownerId == userId) {
            transactions.filter { it.groupId == groupId }
        } else {
            transactions.filter {
                it.groupId == groupId &&
                        (it.status == TransactionStatus.APPROVED ||
                                (it.status == TransactionStatus.PENDING && it.createdBy == userId))
            }
        }
    }

    override fun approveTransaction(transactionId: Int, managerId: Int): Boolean {
        val tx = transactions.find { it.id == transactionId } ?: return false
        val group = groups.find { it.id == tx.groupId } ?: return false

        if (group.ownerId != managerId) return false

        tx.status = TransactionStatus.APPROVED
        tx.approvedBy = managerId
        return true
    }

    override fun rejectTransaction(transactionId: Int, managerId: Int): Boolean {
        val tx = transactions.find { it.id == transactionId } ?: return false
        val group = groups.find { it.id == tx.groupId } ?: return false

        if (group.ownerId != managerId) return false

        tx.status = TransactionStatus.REJECTED
        tx.approvedBy = managerId
        return true
    }

    override fun deleteTransaction(transactionId: Int, managerId: Int): Boolean {
        val tx = transactions.find { it.id == transactionId } ?: return false
        val group = groups.find { it.id == tx.groupId } ?: return false

        if (group.ownerId != managerId) return false

        transactions.remove(tx)
        return true
    }
}