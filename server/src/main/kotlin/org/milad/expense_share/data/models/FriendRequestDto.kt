package org.milad.expense_share.data.models

data class FriendRequestDto(
    val friendId: Int,
    val status: FriendStatus? = null
)