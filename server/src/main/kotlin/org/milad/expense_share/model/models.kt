package org.milad.expense_share.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.database.models.User

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
data class CreateGroupRequest(
    val name: String,
    val memberIds: List<Int> = emptyList()
)

@Serializable
data class AddUserRequest(
    val memberIds: List<Int> = emptyList()
)

@Serializable
data class FriendRequestDto(val phone: String)

@Serializable
data class FriendRequestsResponse(
    val incoming: List<User>,
    val outgoing: List<User>
)

@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val description: String
)

@Serializable
data class ErrorResponse(val error: String, val code: String? = null)

@Serializable
data class SuccessResponse(val message: String, val success: Boolean = true)