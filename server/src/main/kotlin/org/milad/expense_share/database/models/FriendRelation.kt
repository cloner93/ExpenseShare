package org.milad.expense_share.database.models

import kotlinx.serialization.Serializable

@Serializable
enum class FriendRelationStatus{
    PENDING, ACCEPTED, REJECTED
}

@Serializable
data class FriendRelation(
    val userId: Int,
    val friendId: Int,
    var status: FriendRelationStatus
)