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