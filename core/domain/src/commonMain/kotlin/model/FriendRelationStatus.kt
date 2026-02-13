package model

import kotlinx.serialization.Serializable

@Serializable
enum class FriendRelationStatus {
    ACCEPTED, PENDING, REJECTED, BLOCKED,
}
