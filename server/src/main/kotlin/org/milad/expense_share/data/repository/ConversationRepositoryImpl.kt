package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.ConversationMessages
import org.milad.expense_share.data.db.table.ConversationSessions
import org.milad.expense_share.data.models.ConversationMessage
import org.milad.expense_share.data.models.ConversationSession
import org.milad.expense_share.domain.repository.ConversationRepository
import java.util.UUID

class ConversationRepositoryImpl : ConversationRepository {

    override fun createSession(userId: Int, title: String): ConversationSession = transaction {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        ConversationSessions.insert {
            it[ConversationSessions.id] = id
            it[ConversationSessions.userId] = userId
            it[ConversationSessions.title] = title
            it[ConversationSessions.createdAt] = now
            it[ConversationSessions.updatedAt] = now
        }

        ConversationSession(
            id = id,
            userId = userId,
            title = title,
            createdAt = now,
            updatedAt = now
        )
    }

    override fun getUserSessions(userId: Int): List<ConversationSession> = transaction {
        ConversationSessions
            .selectAll()
            .where { ConversationSessions.userId eq userId }
            .orderBy(ConversationSessions.updatedAt to SortOrder.DESC)
            .map { toSession(it) }
    }

    override fun getSession(sessionId: String, userId: Int): ConversationSession? = transaction {
        ConversationSessions
            .selectAll()
            .where { (ConversationSessions.id eq sessionId) and (ConversationSessions.userId eq userId) }
            .map { toSession(it) }
            .firstOrNull()
    }

    override fun saveMessage(
        sessionId: String,
        role: String,
        content: String,
        a2uiPayload: String?
    ): ConversationMessage = transaction {
        val now = System.currentTimeMillis()

        val generatedId = ConversationMessages.insert {
            it[ConversationMessages.sessionId] = sessionId
            it[ConversationMessages.role] = role
            it[ConversationMessages.content] = content
            it[ConversationMessages.a2uiPayload] = a2uiPayload
            it[ConversationMessages.createdAt] = now
        } get ConversationMessages.id

        ConversationSessions.update({ ConversationSessions.id eq sessionId }) {
            it[ConversationSessions.updatedAt] = now
        }

        ConversationMessage(
            id = generatedId,
            sessionId = sessionId,
            role = role,
            content = content,
            a2uiPayload = a2uiPayload,
            createdAt = now
        )
    }

    override fun getMessages(sessionId: String): List<ConversationMessage> = transaction {
        ConversationMessages
            .selectAll()
            .where { ConversationMessages.sessionId eq sessionId }
            .orderBy(ConversationMessages.createdAt to SortOrder.ASC)
            .map { toMessage(it) }
    }

    override fun deleteSession(sessionId: String, userId: Int): Boolean = transaction {
        val session = getSession(sessionId, userId) ?: return@transaction false
        ConversationMessages.deleteWhere { ConversationMessages.sessionId eq sessionId }
        ConversationSessions.deleteWhere { ConversationSessions.id eq sessionId } > 0
    }

    private fun toSession(row: ResultRow) = ConversationSession(
        id = row[ConversationSessions.id],
        userId = row[ConversationSessions.userId],
        title = row[ConversationSessions.title],
        createdAt = row[ConversationSessions.createdAt],
        updatedAt = row[ConversationSessions.updatedAt]
    )

    private fun toMessage(row: ResultRow) = ConversationMessage(
        id = row[ConversationMessages.id],
        sessionId = row[ConversationMessages.sessionId],
        role = row[ConversationMessages.role],
        content = row[ConversationMessages.content],
        a2uiPayload = row[ConversationMessages.a2uiPayload],
        createdAt = row[ConversationMessages.createdAt]
    )
}
