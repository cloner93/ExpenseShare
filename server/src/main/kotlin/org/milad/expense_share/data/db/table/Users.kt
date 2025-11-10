package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100)
    val phone = varchar("phone", 20).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}
