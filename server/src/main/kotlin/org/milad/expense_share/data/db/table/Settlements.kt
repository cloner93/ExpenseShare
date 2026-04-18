package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.milad.expense_share.domain.model.SettlementStatus

object Settlements : Table("settlements") {
    val id          = integer("id").autoIncrement()
    val groupId     = integer("group_id").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val debtorId    = integer("debtor_id").references(Users.id)
    val creditorId  = integer("creditor_id").references(Users.id)
    val amount      = amount("amount")
    val status      = enumerationByName("status", 20, SettlementStatus::class)
    val createdAt   = long("created_at")
    val updatedAt   = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}