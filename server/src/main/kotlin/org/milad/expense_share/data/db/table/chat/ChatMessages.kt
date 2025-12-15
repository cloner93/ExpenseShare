package org.milad.expense_share.data.db.table.chat

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.milad.expense_share.data.db.table.Users

object ChatMessages : Table("chat_messages") {
    val id = integer("id").autoIncrement()
    val sessionId = integer("session_id").references(ChatSessions.id, onDelete = ReferenceOption.CASCADE)
    val senderId = integer("sender_id").references(Users.id).nullable()
    val content = text("content")
    val timestamp = long("timestamp")
    val type = varchar("type", 20)
    val metadata = text("metadata").nullable()

    override val primaryKey = PrimaryKey(id)
}
