package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.ConversationMessage
import org.milad.expense_share.data.models.ConversationSession

interface ConversationRepository {
    fun createSession(userId: Int, title: String): ConversationSession
    fun ensureSession(sessionId: String, userId: Int, title: String = "گفتگوی جدید"): ConversationSession
    fun getUserSessions(userId: Int): List<ConversationSession>
    fun getSession(sessionId: String, userId: Int): ConversationSession?
    fun saveMessage(sessionId: String, role: String, content: String, a2uiPayload: String? = null): ConversationMessage
    fun getMessages(sessionId: String): List<ConversationMessage>
    fun deleteSession(sessionId: String, userId: Int): Boolean
}
