package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.chat.BotContext
import org.milad.expense_share.data.models.chat.ChatMessage
import org.milad.expense_share.data.models.chat.ChatSession

interface ChatRepository {
    fun getOrCreateSession(groupId: Int): ChatSession
    fun getSessionMessages(sessionId: Int, limit: Int = 50): List<ChatMessage>
    fun saveMessage(message: ChatMessage): ChatMessage
    fun updateContext(sessionId: Int, contextData: String): Boolean
    fun getContext(sessionId: Int): BotContext?
}