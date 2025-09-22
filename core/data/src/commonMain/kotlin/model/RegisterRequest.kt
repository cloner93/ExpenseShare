package model

data class RegisterRequest(
    val phone: String,
    val username: String,
    val password: String
)
