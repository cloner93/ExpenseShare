package org.milad.expense_share.database

import org.milad.expense_share.JwtConfig
import org.milad.expense_share.model.AuthResponse
import org.milad.expense_share.model.User

object FakeDatabase {
    private val users = mutableListOf<Pair<User, String>>(
//        User(0,"cloner93","09137511005") to "1234"
    )
    private var lastId = 0

    fun register(username: String, phone: String, password: String): AuthResponse {
        if (users.any { it.first.phone == phone }) {
            return AuthResponse(false, "Phone already registered")
        }
        val user = User(++lastId, username, phone)
        users.add(user to password)

        val token = JwtConfig.generateToken(user)

        return AuthResponse(
            true,
            "Registered successfully",
            token,
            user
        )
    }

    fun login(phone: String, password: String): AuthResponse {
        val match = users.find { it.first.phone == phone && it.second == password }
        return if (match != null) {
            val token = JwtConfig.generateToken(match.first)
            AuthResponse(true, "Login successful", token, match.first)
        } else {
            AuthResponse(false, "Invalid phone or password")
        }
    }
}
