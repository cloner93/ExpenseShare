package model

import kotlinx.serialization.Serializable

@Serializable
data class FriendsListResponse(
    val friends: List<FriendInfo>,
    val total: Int
)