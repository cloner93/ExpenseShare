package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object GroupMembers : Table("group_members") {
    val id = integer("id").autoIncrement()
    val groupId = integer("group_id").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val userId = integer("user_id") references Users.id
    override val primaryKey = PrimaryKey(id)
}