package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class FriendRelationStatus {
    PENDING, ACCEPTED, REJECTED
}

enum class FriendStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    BLOCKED
}

@Serializable
data class FriendRelation(
    val userId: Int,
    val friendId: Int,
    var status: FriendRelationStatus,
)