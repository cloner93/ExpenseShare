package model

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequest(val phone: String)