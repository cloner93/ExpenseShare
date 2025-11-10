package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.Passwords
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.User
import org.milad.expense_share.data.toUser
import org.milad.expense_share.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {
    override fun create(user: User, passwordHash: String): User = transaction {
        val id = Users.insert {
            it[username] = user.username
            it[phone] = user.phone
        } get Users.id


        Passwords.insert {
            it[userId] = id
            it[hash] = passwordHash
        }

        user.copy(id = id)
    }

    override fun findByPhone(phone: String): User? = transaction {
        Users.selectAll()
            .where { Users.phone eq phone }
            .map { it.toUser() }
            .singleOrNull()
    }

    override fun findById(id: Int): User? = transaction {
        Users.selectAll()
            .where { Users.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override fun verifyUser(phone: String, checkPassword: (String) -> Boolean): User? =
        transaction {
            val row = (Users innerJoin Passwords)
                .selectAll().where { Users.phone eq phone }
                .singleOrNull() ?: return@transaction null

            val hash = row[Passwords.hash]
            if (checkPassword(hash)) row.toUser() else null
        }

    override fun lastIndexOfUser(): Int = transaction {
        Users.select(Users.id.max())
            .singleOrNull()?.get(Users.id.max()) ?: 0
    }
}