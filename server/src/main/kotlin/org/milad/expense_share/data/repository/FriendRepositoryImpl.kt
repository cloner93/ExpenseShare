package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.milad.expense_share.data.db.table.FriendRelations
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.data.toUser
import org.milad.expense_share.domain.repository.FriendRepository

class FriendRepositoryImpl : FriendRepository {

    override fun sendFriendRequest(fromId: Int, toPhone: String): Boolean = transaction {
        val toUser = Users.selectAll().where { Users.phone eq toPhone }.singleOrNull()
            ?: return@transaction false

        val alreadyExists = FriendRelations.selectAll().where {
            (FriendRelations.userId eq fromId) and (FriendRelations.friendId eq toUser[Users.id])
        }.any()

        if (alreadyExists) return@transaction false

        FriendRelations.insert {
            it[userId] = fromId
            it[friendId] = toUser[Users.id]
            it[status] = FriendRelationStatus.PENDING.name
        }

        true
    }

    override fun removeFriend(userId: Int, friendPhone: String): Boolean = transaction {
        val friendUser = Users.selectAll().where { Users.phone eq friendPhone }.singleOrNull()
            ?: return@transaction false

        val deleted = FriendRelations.deleteWhere {
            ((FriendRelations.userId eq userId) and (FriendRelations.friendId eq friendUser[Users.id])) or
                    ((FriendRelations.friendId eq userId) and (FriendRelations.userId eq friendUser[Users.id]))
        }
        deleted > 0
    }

    override fun getFriends(userId: Int): List<User> = transaction {
        val friendIds = FriendRelations
            .selectAll().where {
                ((FriendRelations.userId eq userId) or (FriendRelations.friendId eq userId)) and
                        (FriendRelations.status eq FriendRelationStatus.ACCEPTED.name)
            }
            .map {
                if (it[FriendRelations.userId] == userId) it[FriendRelations.friendId]
                else it[FriendRelations.userId]
            }

        Users.selectAll().where { Users.id inList friendIds }.map { it.toUser() }
    }

    override fun rejectFriendRequest(userId: Int, friendPhone: String): Boolean = transaction {
        val friendUser = Users.selectAll().where { Users.phone eq friendPhone }.singleOrNull()
            ?: return@transaction false

        val updated = FriendRelations.update({
            (FriendRelations.userId eq friendUser[Users.id]) and
                    (FriendRelations.friendId eq userId) and
                    (FriendRelations.status eq FriendRelationStatus.PENDING.name)
        }) {
            it[status] = FriendRelationStatus.REJECTED.name
        }

        updated > 0
    }

    override fun acceptFriendRequest(userId: Int, friendPhone: String): Boolean = transaction {
        val friendUser = Users.selectAll().where { Users.phone eq friendPhone }.singleOrNull()
            ?: return@transaction false

        val updated = FriendRelations.update({
            (FriendRelations.userId eq friendUser[Users.id]) and
                    (FriendRelations.friendId eq userId) and
                    (FriendRelations.status eq FriendRelationStatus.PENDING.name)
        }) {
            it[status] = FriendRelationStatus.ACCEPTED.name
        }

        updated > 0
    }

    override fun getIncomingRequests(userId: Int): List<User> = transaction {
        val ids = FriendRelations
            .selectAll().where {
                (FriendRelations.friendId eq userId) and
                        (FriendRelations.status eq FriendRelationStatus.PENDING.name)
            }.map { it[FriendRelations.userId] }

        Users.selectAll().where { Users.id inList ids }.map { it.toUser() }
    }

    override fun getOutgoingRequests(userId: Int): List<User> = transaction {
        val ids = FriendRelations
            .selectAll().where {
                (FriendRelations.userId eq userId) and
                        (FriendRelations.status eq FriendRelationStatus.PENDING.name)
            }.map { it[FriendRelations.friendId] }

        Users.selectAll().where { Users.id inList ids }.map { it.toUser() }
    }
}
