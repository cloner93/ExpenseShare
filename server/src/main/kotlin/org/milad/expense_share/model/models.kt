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
    val members: List<User> = emptyList(),
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
data class CreateGroupRequest(val name: String)