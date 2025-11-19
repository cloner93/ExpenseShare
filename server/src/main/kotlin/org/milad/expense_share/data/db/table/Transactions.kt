package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table
import org.milad.expense_share.data.models.TransactionStatus

object Transactions : Table("transactions") {
    val id = integer("id").autoIncrement()
    val groupId = integer("group_id") references Groups.id
    val title = varchar("title", 255)
    val amount = double("amount")
    val description = text("description")
    val createdBy = integer("created_by") references Users.id
    val status = enumerationByName("status", 20, TransactionStatus::class)
    val approvedBy = integer("approved_by").nullable()
    val createdAt = long("created_at")
    val transactionDate = long("transaction_date")
    override val primaryKey = PrimaryKey(id)
}

object TransactionPayers : Table("transaction_payers") {
    val id = integer("id").autoIncrement()
    val transactionId = integer("transaction_id") references Transactions.id
    val userId = integer("user_id") references Users.id
    val amountPaid = double("amount_paid")

    override val primaryKey = PrimaryKey(id)
}

object TransactionShares : Table("transaction_shares") {
    val id = integer("id").autoIncrement()
    val transactionId = integer("transaction_id") references Transactions.id
    val type = varchar("type", 20)

    override val primaryKey = PrimaryKey(id)
}

object TransactionShareMembers : Table("transaction_share_members") {
    val id = integer("id").autoIncrement()
    val shareId = integer("share_id") references TransactionShares.id
    val userId = integer("user_id") references Users.id
    val share = double("share")

    override val primaryKey = PrimaryKey(id)
}