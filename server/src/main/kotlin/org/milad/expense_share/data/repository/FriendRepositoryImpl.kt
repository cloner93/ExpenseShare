package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.FriendRelations
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.FriendInfo
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

        val now = System.currentTimeMillis()

        FriendRelations.insert {
            it[userId] = fromId
            it[friendId] = toUser[Users.id]
            it[status] = FriendRelationStatus.PENDING.name
            it[requestedBy] = fromId
            it[createdAt] = now
            it[updatedAt] = now
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

        val now = System.currentTimeMillis()

        val updated = FriendRelations.update({
            (FriendRelations.userId eq friendUser[Users.id]) and
                    (FriendRelations.friendId eq userId) and
                    (FriendRelations.status eq FriendRelationStatus.PENDING.name)
        }) {
            it[status] = FriendRelationStatus.REJECTED.name
            it[updatedAt] = now
        }

        updated > 0
    }

    override fun acceptFriendRequest(userId: Int, friendPhone: String): Boolean = transaction {
        val friendUser = Users.selectAll().where { Users.phone eq friendPhone }.singleOrNull()
            ?: return@transaction false

        val now = System.currentTimeMillis()

        val updated = FriendRelations.update({
            (FriendRelations.userId eq friendUser[Users.id]) and
                    (FriendRelations.friendId eq userId) and
                    (FriendRelations.status eq FriendRelationStatus.PENDING.name)
        }) {
            it[status] = FriendRelationStatus.ACCEPTED.name
            it[updatedAt] = now
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

    override fun getFriendsWithStatus(
        userId: Int,
        status: FriendRelationStatus?,
    ): List<FriendInfo> = transaction {

        val baseQuery = FriendRelations
            .innerJoin(Users, { friendId }, { Users.id })
            .selectAll()
            .where {
                ((FriendRelations.userId eq userId) or (FriendRelations.friendId eq userId))
            }


        val filteredQuery = if (status != null) {
            baseQuery.andWhere { FriendRelations.status eq status.name }
        } else {
            baseQuery
        }


        filteredQuery.map { row ->
            val isUserInitiator = row[FriendRelations.userId] == userId
            val friendUserId = if (isUserInitiator) {
                row[FriendRelations.friendId]
            } else {
                row[FriendRelations.userId]
            }


            val friendUser = Users.selectAll()
                .where { Users.id eq friendUserId }
                .single()
                .toUser()

            FriendInfo(
                user = friendUser,
                status = FriendRelationStatus.valueOf(row[FriendRelations.status]),
                requestedBy = row[FriendRelations.requestedBy],
                createdAt = row[FriendRelations.createdAt],
                updatedAt = row[FriendRelations.updatedAt]
            )
        }
    }

    override fun getIncomingRequestsWithStatus(userId: Int): List<FriendInfo> = transaction {
        FriendRelations
            .innerJoin(Users, { FriendRelations.userId }, { Users.id })
            .selectAll()
            .where {
                (FriendRelations.friendId eq userId) and
                        (FriendRelations.status eq FriendRelationStatus.PENDING.name)
            }
            .map { row ->
                FriendInfo(
                    user = row.toUser(),
                    status = FriendRelationStatus.PENDING,
                    requestedBy = row[FriendRelations.requestedBy],
                    createdAt = row[FriendRelations.createdAt],
                    updatedAt = row[FriendRelations.updatedAt]
                )
            }
    }

    override fun getOutgoingRequestsWithStatus(userId: Int): List<FriendInfo> = transaction {
        FriendRelations
            .innerJoin(Users, { FriendRelations.friendId }, { Users.id })
            .selectAll()
            .where {
                (FriendRelations.userId eq userId) and
                        (FriendRelations.status eq FriendRelationStatus.PENDING.name)
            }
            .map { row ->
                FriendInfo(
                    user = row.toUser(),
                    status = FriendRelationStatus.PENDING,
                    requestedBy = row[FriendRelations.requestedBy],
                    createdAt = row[FriendRelations.createdAt],
                    updatedAt = row[FriendRelations.updatedAt]
                )
            }
    }

    override fun blockFriend(userId: Int, friendPhone: String): Boolean = transaction {
        val friendUser = Users.selectAll().where { Users.phone eq friendPhone }.singleOrNull()
            ?: return@transaction false

        val now = System.currentTimeMillis()

        val updated = FriendRelations.update({
            ((FriendRelations.userId eq userId) and (FriendRelations.friendId eq friendUser[Users.id])) or
                    ((FriendRelations.friendId eq userId) and (FriendRelations.userId eq friendUser[Users.id]))
        }) {
            it[status] = FriendRelationStatus.BLOCKED.name
            it[updatedAt] = now
        }

        updated > 0
    }

    override fun unblockFriend(userId: Int, friendPhone: String): Boolean = transaction {
        val friendUser = Users.selectAll().where { Users.phone eq friendPhone }.singleOrNull()
            ?: return@transaction false

        val now = System.currentTimeMillis()

        val updated = FriendRelations.update({
            ((FriendRelations.userId eq userId) and (FriendRelations.friendId eq friendUser[Users.id])) or
                    ((FriendRelations.friendId eq userId) and (FriendRelations.userId eq friendUser[Users.id]))
        }) {
            it[status] = FriendRelationStatus.ACCEPTED.name
            it[updatedAt] = now
        }

        updated > 0
    }

    override fun getBlockedFriends(userId: Int): List<FriendInfo> = transaction {
        getFriendsWithStatus(userId, FriendRelationStatus.BLOCKED)
    }

    override fun getFriendsByStatus(userId: Int, status: FriendRelationStatus): List<User> =
        transaction {
            val friendIds = FriendRelations
                .selectAll()
                .where {
                    ((FriendRelations.userId eq userId) or (FriendRelations.friendId eq userId)) and
                            (FriendRelations.status eq status.name)
                }
                .map {
                    if (it[FriendRelations.userId] == userId) it[FriendRelations.friendId]
                    else it[FriendRelations.userId]
                }

            Users.selectAll().where { Users.id inList friendIds }.map { it.toUser() }
        }
}