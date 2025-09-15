package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.model.AuthResponse
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.security.JwtConfig
import org.mindrot.jbcrypt.BCrypt

class AuthService(
    private val userRepository: UserRepository,
) {
    fun register(username: String, phone: String, password: String): Result<AuthResponse> {
        return try {
            if (userRepository.findByPhone(phone) != null) {
                return Result.failure(IllegalArgumentException("Phone already registered"))
            }

            val passwordHash = hashPassword(password)
            val user = User(
                id = userRepository.lastIndexOfUser() + 1,
                username = username,
                phone = phone
            )
            userRepository.create(user, passwordHash)

            val token = JwtConfig.generateToken(user)
            Result.success(
                AuthResponse(
                    token = token,
                    user = user
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun login(phone: String, password: String): Result<AuthResponse> {
        return try {
            val user =
                userRepository.verifyUser(phone, checkPassword = { checkPassword(password, it) })
                    ?: return Result.failure(IllegalArgumentException("Invalid phone or password"))

            val token = JwtConfig.generateToken(user)
            Result.success(
                AuthResponse(
                    token = token,
                    user = user
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String =
        BCrypt.hashpw(password, BCrypt.gensalt())

    private fun checkPassword(password: String, hash: String): Boolean =
        BCrypt.checkpw(password, hash)
}