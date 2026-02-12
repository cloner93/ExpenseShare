package org.milad.expense_share.presentation.friends.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.data.models.FriendInfo

@Serializable
data class FriendsListResponse(
    val friends: List<FriendInfo>,
    val total: Int
)