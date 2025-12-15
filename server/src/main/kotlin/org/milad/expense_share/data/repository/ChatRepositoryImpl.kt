package org.milad.expense_share.data.repository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.db.table.chat.BotContexts
import org.milad.expense_share.data.db.table.chat.ChatMessages
import org.milad.expense_share.data.db.table.chat.ChatSessions
import org.milad.expense_share.data.models.chat.BotContext
import org.milad.expense_share.data.models.chat.ChatMessage
import org.milad.expense_share.data.models.chat.ChatSession
import org.milad.expense_share.data.models.chat.MessageType
import org.milad.expense_share.domain.repository.ChatRepository

class ChatRepositoryImpl : ChatRepository {

    override fun getOrCreateSession(groupId: Int): ChatSession = transaction {
        val existing = ChatSessions
            .selectAll()
            .where { ChatSessions.groupId eq groupId }
            .orderBy(ChatSessions.createdAt, SortOrder.DESC)
            .limit(1)
            .firstOrNull()

        if (existing != null) {
            return@transaction ChatSession(
                id = existing[ChatSessions.id],
                groupId = existing[ChatSessions.groupId],
                createdAt = existing[ChatSessions.createdAt],
                updatedAt = existing[ChatSessions.updatedAt]
            )
        }

        val now = System.currentTimeMillis()
        val sessionId = ChatSessions.insert {
            it[ChatSessions.groupId] = groupId
            it[ChatSessions.createdAt] = now
            it[ChatSessions.updatedAt] = now
        } get ChatSessions.id

        ChatSession(
            id = sessionId,
            groupId = groupId,
            createdAt = now,
            updatedAt = now
        )
    }

    override fun getSessionMessages(sessionId: Int, limit: Int): List<ChatMessage> = transaction {
        ChatMessages
            .leftJoin(Users, { ChatMessages.senderId }, { Users.id })
            .selectAll()
            .where { ChatMessages.sessionId eq sessionId }
            .orderBy(ChatMessages.timestamp, SortOrder.ASC)
            .limit(limit)
            .map {
                ChatMessage(
                    id = it[ChatMessages.id],
                    sessionId = it[ChatMessages.sessionId],
                    senderId = it[ChatMessages.senderId],
                    senderName = it.getOrNull(Users.username),
                    content = it[ChatMessages.content],
                    timestamp = it[ChatMessages.timestamp],
                    type = MessageType.valueOf(it[ChatMessages.type]),
                    metadata = it[ChatMessages.metadata]
                )
            }
    }

    override fun saveMessage(message: ChatMessage): ChatMessage = transaction {
        val messageId = ChatMessages.insert {
            it[ChatMessages.sessionId] = message.sessionId
            it[ChatMessages.senderId] = message.senderId
            it[ChatMessages.content] = message.content
            it[ChatMessages.timestamp] = message.timestamp
            it[ChatMessages.type] = message.type.name
            it[ChatMessages.metadata] = message.metadata
        } get ChatMessages.id

        ChatSessions.update({ ChatSessions.id eq message.sessionId }) {
            it[ChatSessions.updatedAt] = message.timestamp
        }

        message.copy(id = messageId)
    }

    override fun updateContext(sessionId: Int, contextData: String): Boolean = transaction {
        val existing = BotContexts
            .selectAll()
            .where { BotContexts.sessionId eq sessionId }
            .singleOrNull()

        if (existing != null) {
            BotContexts.update({ BotContexts.sessionId eq sessionId }) {
                it[BotContexts.contextData] = contextData
                it[BotContexts.lastUpdated] = System.currentTimeMillis()
            } > 0
        } else {
            BotContexts.insert {
                it[BotContexts.sessionId] = sessionId
                it[BotContexts.contextData] = contextData
                it[BotContexts.lastUpdated] = System.currentTimeMillis()
            }
            true
        }
    }

    override fun getContext(sessionId: Int): BotContext? = transaction {
        BotContexts
            .selectAll()
            .where { BotContexts.sessionId eq sessionId }
            .singleOrNull()
            ?.let {
                BotContext(
                    id = it[BotContexts.id],
                    sessionId = it[BotContexts.sessionId],
                    contextData = it[BotContexts.contextData],
                    lastUpdated = it[BotContexts.lastUpdated]
                )
            }
    }
}