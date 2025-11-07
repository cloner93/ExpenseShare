package model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val members: List<User>,
    val transactions: List<Transaction>
)