package org.milad.expense_share.domain.service

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.milad.expense_share.data.models.User
import org.milad.expense_share.data.models.chat.ChatMessage
import org.milad.expense_share.data.models.chat.ConversationContext
import org.milad.expense_share.data.models.chat.MessageType
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository

class BotService(
    private val httpClient: HttpClient,
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val apiKey: String,
    private val apiUrl: String,
    private val model: String
) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    suspend fun processMessage(
        userId: Int,
        groupId: Int,
        userMessage: String,
        conversationHistory: List<ChatMessage>
    ): String {
        val context = buildGroupContext(groupId, userId)
        
        val systemPrompt = buildSystemPrompt(context)
        
        val messages = mutableListOf<OpenRouterMessage>()
        messages.add(OpenRouterMessage("system", systemPrompt))
        
        conversationHistory.takeLast(10).forEach { msg ->
            val role = when (msg.type) {
                MessageType.USER -> "user"
                MessageType.BOT -> "assistant"
                MessageType.SYSTEM -> "system"
            }
            messages.add(OpenRouterMessage(role, msg.content))
        }
        
        messages.add(OpenRouterMessage("user", userMessage))
        
        return try {
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "https://expenseshare.app")
                header("X-Title", "ExpenseShare Bot")
                
                setBody(json.encodeToString(
                    OpenRouterRequest.serializer(),
                    OpenRouterRequest(
                        model = model,
                        messages = messages
                    )
                ))
            }
            
            val responseBody = response.bodyAsText()
            val openRouterResponse = json.decodeFromString<OpenRouterResponse>(responseBody)
            
            openRouterResponse.choices.firstOrNull()?.message?.content
                ?: "I'm having trouble processing that. Could you try rephrasing?"
                
        } catch (e: Exception) {
            "Sorry, I'm experiencing technical difficulties. Please try again later."
        }
    }
    
    private fun buildSystemPrompt(context: ConversationContext): String {
        val members = context.members.joinToString(", ") { it.username }
        val totalTransactions = context.recentTransactions.size
        
        return """
You are ExpenseShare Bot, a helpful assistant for managing group expenses.

**Current Group Context:**
- Group: ${context.groupName}
- Members: $members
- Recent Transactions: $totalTransactions

**Your Capabilities:**
1. Answer questions about group expenses and balances
2. Help users understand who owes what
3. Assist with transaction creation (gather details, then provide structured response)
4. Provide expense summaries and insights

**Important Rules:**
- Be concise and friendly
- Use actual data from the context when available
- When creating transactions, ask for: title, amount, description, payers, and how to split
- Format amounts clearly (e.g., $50.00)
- If you need more information, ask specific questions
- Keep responses under 200 words

**Response Format for Transaction Creation:**
When user wants to add a transaction, gather all details then respond with:
```
TRANSACTION_REQUEST
Title: [title]
Amount: [amount]
Description: [description]
```

**Examples:**
User: "Show me our expenses"
Bot: "Your group has $totalTransactions recent transactions. Would you like a detailed breakdown?"

User: "Who owes money?"
Bot: "Let me check the balances... [analyze and respond]"

User: "Add lunch expense $50"
Bot: "I'll help you add that. Who paid for lunch? And should it be split equally among all ${context.members.size} members?"

Now respond to user messages naturally and helpfully.
        """.trimIndent()
    }
    
    private fun buildGroupContext(groupId: Int, userId: Int): ConversationContext {
        val memberIds = groupRepository.getUsersOfGroup(groupId)
        val members = memberIds.map { id ->
            User(id, "Member$id", "phone$id")
        }
        
        val transactions = transactionRepository.getTransactions(userId, groupId)
            .take(10)
        
        return ConversationContext(
            groupId = groupId,
            groupName = "Group $groupId",
            members = members,
            recentTransactions = transactions,
            messageHistory = emptyList()
        )
    }

    fun parseTransactionRequest(botResponse: String): TransactionIntent? {
        if (!botResponse.contains("TRANSACTION_REQUEST")) return null
        
        val lines = botResponse.lines()
        var title: String? = null
        var amount: Double? = null
        var description: String? = null
        
        lines.forEach { line ->
            when {
                line.startsWith("Title:") -> title = line.substringAfter("Title:").trim()
                line.startsWith("Amount:") -> {
                    val amountStr = line.substringAfter("Amount:").trim()
                        .replace("$", "")
                        .replace(",", "")
                    amount = amountStr.toDoubleOrNull()
                }
                line.startsWith("Description:") -> description = line.substringAfter("Description:").trim()
            }
        }
        
        return if (title != null && amount != null) {
            TransactionIntent(title, amount, description ?: "")
        } else null
    }
}

data class TransactionIntent(
    val title: String,
    val amount: Double,
    val description: String
)

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>
) {
    @Serializable
    data class Choice(
        val message: OpenRouterMessage
    )
}