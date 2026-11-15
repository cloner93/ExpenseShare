package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table

object ConversationSessions : Table("conversation_sessions") {
    val id = varchar("id", 64)
    val userId = integer("user_id") references Users.id
    val title = varchar("title", 255)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object ConversationMessages : Table("conversation_messages") {
    val id = integer("id").autoIncrement()
    val sessionId = varchar("session_id", 64) references ConversationSessions.id
    val role = varchar("role", 20)
    val content = text("content")
    val a2uiPayload = text("a2ui_payload").nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
