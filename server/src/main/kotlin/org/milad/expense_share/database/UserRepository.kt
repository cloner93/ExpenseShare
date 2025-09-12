package org.milad.expense_share.database

import org.milad.expense_share.JwtConfig
import org.milad.expense_share.database.FakeDatabase.users
import org.milad.expense_share.database.models.User
import org.milad.expense_share.model.AuthResponse

interface UserRepository {
    fun register(username: String, phone: String, password: String): AuthResponse
    fun login(phone: String, password: String): AuthResponse
}

class InMemoryUserRepository : UserRepository {
    override fun register(
        username: String,
        phone: String,
        password: String
    ): AuthResponse {
        if (users.any { it.first.phone == phone }) {
            return AuthResponse(false, "Phone already registered")
        }
        val user = User(users.size + 1, username, phone)
        users.add(user to password)

        val token = JwtConfig.generateToken(user)

        return AuthResponse(
            true, "Registered successfully", token, user
        )
    }

    override fun login(
        phone: String,
        password: String
    ): AuthResponse {
        val match = users.find { it.first.phone == phone && it.second == password }
        return if (match != null) {
            val token = JwtConfig.generateToken(match.first)
            AuthResponse(true, "Login successful", token, match.first)
        } else {
            AuthResponse(false, "Invalid phone or password")
        }
    }
}