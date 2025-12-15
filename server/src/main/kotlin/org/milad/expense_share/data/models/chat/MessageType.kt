package org.milad.expense_share.data.models.chat

import kotlinx.serialization.Serializable
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.User

@Serializable
enum class MessageType {
    USER,
    BOT,
    SYSTEM
}

@Serializable
data class ChatSession(
    val id: Int,
    val groupId: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class ChatMessage(
    val id: Int,
    val sessionId: Int,
    val senderId: Int?,
    val senderName: String?,
    val content: String,
    val timestamp: Long,
    val type: MessageType,
    val metadata: String? = null,
)

@Serializable
data class BotContext(
    val id: Int,
    val sessionId: Int,
    val contextData: String,
    val lastUpdated: Long,
)

@Serializable
data class SendMessageRequest(
    val content: String,
)

@Serializable
data class ChatHistoryResponse(
    val session: String,
    val messages: List<ChatMessage>,
)

@Serializable
data class ConversationContext(
    val groupId: Int,
    val groupName: String,
    val members: List<User>,
    val recentTransactions: List<Transaction>,
    val messageHistory: List<ChatMessage>,
)