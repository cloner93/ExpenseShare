package org.milad.expense_share.domain.service

import org.milad.expense_share.security.JwtConfig
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.model.AuthResponse
import org.milad.expense_share.domain.repository.UserRepository

class AuthService(
    private val userRepository: UserRepository,
) {
    suspend fun register(username: String, phone: String, password: String): AuthResponse {
        if (userRepository.findByPhone(phone) != null) {
            return AuthResponse(false, "Phone already registered")
        }

        val passwordHash = hashPassword(password)
        val user =
            User(id = userRepository.lastIndexOfUser() + 1, username = username, phone = phone)
        userRepository.create(user, passwordHash)

        val token = JwtConfig.generateToken(user)
        return AuthResponse(true, "Registered successfully", token, user)
    }

    suspend fun login(phone: String, password: String): AuthResponse {
        val passwordHash = hashPassword(password)
        val user = userRepository.verifyUser(phone, passwordHash)
            ?: return AuthResponse(false, "Invalid phone or password")

        val token = JwtConfig.generateToken(user)
        return AuthResponse(true, "Login successful", token, user)
    }

    private fun hashPassword(password: String): String {
        return password
    }
}