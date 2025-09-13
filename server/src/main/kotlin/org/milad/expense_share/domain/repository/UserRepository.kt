package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.User

interface UserRepository {
    suspend fun create(user: User, passwordHash: String): User
    suspend fun findByPhone(phone: String): User?
    suspend fun verifyUser(phone: String, passwordHash: String): User?
    suspend fun lastIndexOfUser():Int
}