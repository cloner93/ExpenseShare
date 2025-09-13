package org.milad.expense_share.data.repository

import org.milad.expense_share.data.db.FakeDatabase.users
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.UserRepository

class InMemoryUserRepository : UserRepository {
    override suspend fun create(user: User, passwordHash: String): User {
        users.add(user to passwordHash)
        return user
    }

    override suspend fun findByPhone(phone: String): User? {
        return users.firstOrNull { it.first.phone == phone }?.first
    }

    override suspend fun verifyUser(phone: String, passwordHash: String): User? {
        return users.find { it.first.phone == phone && it.second == passwordHash }?.first
    }

    override suspend fun lastIndexOfUser(): Int {
        return users.size
    }
}