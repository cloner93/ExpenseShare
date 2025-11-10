package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.milad.expense_share.data.db.table.Groups
import org.milad.expense_share.data.db.table.Transactions
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.data.toTransaction
import org.milad.expense_share.domain.repository.TransactionRepository

class TransactionRepositoryImpl : TransactionRepository {

    override fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Double,
        description: String,
    ): Transaction? = transaction {
        val group = Groups.selectAll().where { Groups.id eq groupId }.singleOrNull()
            ?: return@transaction null
        val ownerId = group[Groups.ownerId]

        val status =
            if (ownerId == userId) TransactionStatus.APPROVED else TransactionStatus.PENDING
        val approvedBy = if (status == TransactionStatus.APPROVED) userId else null

        val id = Transactions.insert {
            it[Transactions.groupId] = groupId
            it[Transactions.title] = title
            it[Transactions.amount] = amount
            it[Transactions.description] = description
            it[Transactions.createdBy] = userId
            it[Transactions.status] = status
            it[Transactions.approvedBy] = approvedBy
            it[Transactions.createdAt] = System.currentTimeMillis()
            it[Transactions.transactionDate] = System.currentTimeMillis()
        } get Transactions.id

        Transaction(
            id = id,
            groupId = groupId,
            title = title,
            amount = amount,
            description = description,
            createdBy = userId,
            status = status,
            approvedBy = approvedBy
        )
    }

    override fun getTransactions(userId: Int, groupId: Int): List<Transaction> = transaction {
        val group = Groups.selectAll().where { Groups.id eq groupId }.singleOrNull()
            ?: return@transaction emptyList()
        val ownerId = group[Groups.ownerId]

        val query = if (ownerId == userId) {
            Transactions.selectAll().where { Transactions.groupId eq groupId }
        } else {
            Transactions.selectAll().where {
                (Transactions.groupId eq groupId) and ((Transactions.status eq TransactionStatus.APPROVED) or
                        ((Transactions.status eq TransactionStatus.PENDING) and (Transactions.createdBy eq userId)))
            }
        }

        query.map { it.toTransaction() }
    }

    override fun approveTransaction(transactionId: Int, managerId: Int): Boolean = transaction {
        val tx = Transactions.selectAll().where{ Transactions.id eq transactionId }.singleOrNull()
            ?: return@transaction false
        val group = Groups.selectAll().where { Groups.id eq tx[Transactions.groupId] }.singleOrNull()
            ?: return@transaction false

        if (group[Groups.ownerId] != managerId) return@transaction false

        Transactions.update({ Transactions.id eq transactionId }) {
            it[status] = TransactionStatus.APPROVED
            it[approvedBy] = managerId
        }
        true
    }

    override fun rejectTransaction(transactionId: Int, managerId: Int): Boolean = transaction {
        val tx = Transactions.selectAll().where { Transactions.id eq transactionId }.singleOrNull()
            ?: return@transaction false
        val group =
            Groups.selectAll().where { Groups.id eq tx[Transactions.groupId] }.singleOrNull()
                ?: return@transaction false

        if (group[Groups.ownerId] != managerId) return@transaction false

        Transactions.update({ Transactions.id eq transactionId }) {
            it[status] = TransactionStatus.REJECTED
            it[approvedBy] = managerId
        }
        true
    }

    override fun deleteTransaction(transactionId: Int, managerId: Int): Boolean = transaction {
        val tx = Transactions.selectAll().where { Transactions.id eq transactionId }.singleOrNull()
            ?: return@transaction false
        val group =
            Groups.selectAll().where { Groups.id eq tx[Transactions.groupId] }.singleOrNull()
                ?: return@transaction false

        if (group[Groups.ownerId] != managerId) return@transaction false
        Transactions.deleteWhere { Transactions.id eq transactionId } > 0
    }
}
