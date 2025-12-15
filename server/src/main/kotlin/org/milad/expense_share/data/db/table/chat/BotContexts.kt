package org.milad.expense_share.data.db.table.chat

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BotContexts : Table("bot_contexts") {
    val id = integer("id").autoIncrement()
    val sessionId = integer("session_id").references(ChatSessions.id, onDelete = ReferenceOption.CASCADE)
    val contextData = text("context_data")
    val lastUpdated = long("last_updated")
    
    override val primaryKey = PrimaryKey(id)
}