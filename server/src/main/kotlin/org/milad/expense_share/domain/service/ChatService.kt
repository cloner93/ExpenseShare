package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.chat.ChatMessage
import org.milad.expense_share.data.models.chat.ChatSession
import org.milad.expense_share.data.models.chat.MessageType
import org.milad.expense_share.domain.repository.ChatRepository
import org.milad.expense_share.domain.repository.UserRepository

class ChatService(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val botService: BotService,
) {

    fun getOrCreateChatSession(groupId: Int): ChatSession {
        return chatRepository.getOrCreateSession(groupId)
    }

    fun getChatHistory(sessionId: Int, limit: Int = 50): List<ChatMessage> {
        return chatRepository.getSessionMessages(sessionId, limit)
    }

    suspend fun handleUserMessage(
        userId: Int,
        groupId: Int,
        messageContent: String,
    ): Result<List<ChatMessage>> {
        return try {
            val session = getOrCreateChatSession(groupId)

            val userMessage = ChatMessage(
                id = 0,
                sessionId = session.id,
                senderId = userId,
                senderName = userRepository.findById(userId)?.username,
                content = messageContent,
                timestamp = System.currentTimeMillis(),
                type = MessageType.USER
            )

            val savedUserMessage = chatRepository.saveMessage(userMessage)

            val history = getChatHistory(session.id)

            val botResponse = botService.processMessage(
                userId = userId,
                groupId = groupId,
                userMessage = messageContent,
                conversationHistory = history
            )

            val transactionIntent = botService.parseTransactionRequest(botResponse)
            val finalBotResponse = if (transactionIntent != null) {
                """
                I've understood your transaction request:
                
                📝 **${transactionIntent.title}**
                💵 Amount: $${transactionIntent.amount}
                📄 ${transactionIntent.description}
                
                To complete this, please use the "Add Transaction" button and fill in the payer and split details, or tell me more specifics!
                """.trimIndent()
            } else {
                botResponse
            }

            val botMessage = ChatMessage(
                id = 0,
                sessionId = session.id,
                senderId = null,
                senderName = "ExpenseShare Bot",
                content = finalBotResponse,
                timestamp = System.currentTimeMillis(),
                type = MessageType.BOT,
                metadata = transactionIntent?.let {
                    """{"transactionIntent": true, "title": "${it.title}", "amount": ${it.amount}}"""
                }
            )

            val savedBotMessage = chatRepository.saveMessage(botMessage)
            Result.success(listOf(savedUserMessage, savedBotMessage))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendSystemMessage(
        groupId: Int,
        message: String,
    ): Result<ChatMessage> {
        return try {
            val session = getOrCreateChatSession(groupId)

            val systemMessage = ChatMessage(
                id = 0,
                sessionId = session.id,
                senderId = null,
                senderName = "System",
                content = message,
                timestamp = System.currentTimeMillis(),
                type = MessageType.SYSTEM
            )

            val saved = chatRepository.saveMessage(systemMessage)
            Result.success(saved)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}