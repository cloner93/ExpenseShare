package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.TransactionStatus
import java.sql.ResultSet

object Transactions : Table("transactions") {
    val id = integer("id").autoIncrement()
    val groupId = integer("group_id").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val amount = amount("amount")
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
    val transactionId =
        integer("transaction_id").references(Transactions.id, onDelete = ReferenceOption.CASCADE)
    val userId = integer("user_id") references Users.id
    val amountPaid = amount("amount_paid")

    override val primaryKey = PrimaryKey(id)
}

object TransactionShares : Table("transaction_shares") {
    val id = integer("id").autoIncrement()
    val transactionId =
        integer("transaction_id").references(Transactions.id, onDelete = ReferenceOption.CASCADE)
    val type = varchar("type", 20)

    override val primaryKey = PrimaryKey(id)
}

object TransactionShareMembers : Table("transaction_share_members") {
    val id = integer("id").autoIncrement()
    val shareId =
        integer("share_id").references(TransactionShares.id, onDelete = ReferenceOption.CASCADE)
    val userId = integer("user_id") references Users.id
    val share = amount("share")

    override val primaryKey = PrimaryKey(id)
}

class AmountColumnType : ColumnType<Amount>() {
    override fun sqlType(): String = "BIGINT"

    override fun valueFromDB(value: Any): Amount = when (value) {
        is Long -> Amount(value)
        is Number -> Amount(value.toLong())
        else -> error("Unexpected value of type ${value::class} for Amount")
    }

    override fun notNullValueToDB(value: Amount): Any = value.value

    override fun readObject(rs: ResultSet, index: Int): Any = rs.getLong(index)
}

fun Table.amount(name: String): Column<Amount> = registerColumn(name, AmountColumnType())