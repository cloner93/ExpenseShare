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