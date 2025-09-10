package org.milad.expense_share.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val phone: String, val password: String)
@Serializable
data class LoginRequest(val phone: String, val password: String)
@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: User? = null
)

@Serializable
data class User(val id: Int, val username: String, val phone: String)

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val members: MutableList<Int>,
    val totalDebt: Double = 0.0,
    val totalCredit: Double = 0.0
)

@Serializable
data class DashboardData(
    val groups: List<Group> = emptyList(),
    val totalDebt: Double = 0.0,
    val totalCredit: Double = 0.0
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val memberIds: List<Int> = emptyList()
)


@Serializable
data class AddUserRequest(
    val memberIds: List<Int> = emptyList()
)

@Serializable
data class FriendRelation(
    val userId: Int,
    val friendId: Int,
    var status: String // "pending", "accepted", "rejected"
)

@Serializable
data class FriendRequestDto(val phone: String)

@Serializable
data class FriendRequestsResponse(
    val incoming: List<User>,
    val outgoing: List<User>
)

@Serializable
enum class TransactionStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class Transaction(
    val id: Int,
    val groupId: Int,
    val title: String,
    val amount: Double,
    val description: String,
    val createdBy: Int,
    var status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val transactionDate: Long = System.currentTimeMillis(),
    var approvedBy: Int? = null
)

@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val description: String
)