package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table

object Groups : Table("groups") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val ownerId = integer("owner_id") references Users.id
    override val primaryKey = PrimaryKey(id)
}