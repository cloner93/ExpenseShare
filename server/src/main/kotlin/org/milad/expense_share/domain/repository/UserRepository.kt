package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.User

interface UserRepository {
    fun create(user: User, passwordHash: String): User
    fun findByPhone(phone: String): User?
    fun findById(id: Int): User?
    fun verifyUser(phone: String, passwordHash: String): User?
    fun lastIndexOfUser(): Int
}