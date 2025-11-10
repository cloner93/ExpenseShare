package org.milad.expense_share.data.inMemoryRepository

import org.milad.expense_share.data.db.FakeDatabase.users
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.UserRepository

class InMemoryUserRepository : UserRepository {
    override fun create(user: User, passwordHash: String): User {
        users.add(user to passwordHash)
        return user
    }

    override fun findByPhone(phone: String): User? {
        return users.firstOrNull { it.first.phone == phone }?.first
    }

    override fun findById(id: Int): User? {
        return users.firstOrNull { it.first.id == id }?.first
    }

    override fun verifyUser(phone: String, checkPassword: (String) -> Boolean): User? {
        return users.find { it.first.phone == phone && checkPassword(it.second) }?.first
    }

    override fun lastIndexOfUser(): Int {
        return users.size
    }
}