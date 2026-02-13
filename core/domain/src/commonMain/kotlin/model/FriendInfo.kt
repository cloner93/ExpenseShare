package model

import kotlinx.serialization.Serializable

@Serializable
data class FriendInfo(
    val user: User,
    val status: FriendRelationStatus,
    val requestedBy: Int,
    val createdAt: Long,
    val updatedAt: Long
)