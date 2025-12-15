package org.milad.expense_share.data.db.table.chat

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.milad.expense_share.data.db.table.Groups

object ChatSessions : Table("chat_sessions") {
    val id = integer("id").autoIncrement()
    val groupId = integer("group_id").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    
    override val primaryKey = PrimaryKey(id)
}
