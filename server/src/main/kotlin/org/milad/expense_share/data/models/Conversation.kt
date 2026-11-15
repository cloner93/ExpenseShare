package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ConversationSession(
    val id: String,
    val userId: Int,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ConversationMessage(
    val id: Int,
    val sessionId: String,
    val role: String,
    val content: String,
    val a2uiPayload: String? = null,
    val createdAt: Long
)
