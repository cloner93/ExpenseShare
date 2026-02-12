package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.milad.expense_share.data.db.table.FriendRelations
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.FriendInfo
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.FriendDirection
import org.milad.expense_share.domain.repository.FriendRepository

class FriendRepositoryImpl : FriendRepository {
    override fun getAllFriends(
        userId: Int,
        status: FriendRelationStatus?,
        direction: FriendDirection?
    ): List<FriendInfo> = transaction {


        var query1 = FriendRelations
            .innerJoin(Users, { FriendRelations.friendId }, { Users.id })
            .selectAll()
            .where { FriendRelations.userId eq userId }


        var query2 = FriendRelations
            .innerJoin(Users, { FriendRelations.userId }, { Users.id })
            .selectAll()
            .where { FriendRelations.friendId eq userId }


        if (status != null) {
            query1 = query1.andWhere { FriendRelations.status eq status.name }
            query2 = query2.andWhere { FriendRelations.status eq status.name }
        }


        val results = when {
            direction == FriendDirection.INCOMING && status == FriendRelationStatus.PENDING -> {

                query2.map { mapRowToFriendInfo(it, userId, isUserSender = false) }
            }

            direction == FriendDirection.OUTGOING && status == FriendRelationStatus.PENDING -> {

                query1.map { mapRowToFriendInfo(it, userId, isUserSender = true) }
            }

            else -> {

                val results1 = query1.map { mapRowToFriendInfo(it, userId, isUserSender = true) }
                val results2 = query2.map { mapRowToFriendInfo(it, userId, isUserSender = false) }
                results1 + results2
            }
        }

        results
    }

    override fun sendFriendRequest(fromUserId: Int, toUserPhone: String): Boolean = transaction {

        val toUser = Users
            .selectAll()
            .where { Users.phone eq toUserPhone }
            .singleOrNull()
            ?: return@transaction false

        val toUserId = toUser[Users.id]


        val exists = FriendRelations
            .selectAll()
            .where {
                ((FriendRelations.userId eq fromUserId) and (FriendRelations.friendId eq toUserId)) or
                        ((FriendRelations.userId eq toUserId) and (FriendRelations.friendId eq fromUserId))
            }
            .any()

        if (exists) return@transaction false


        val now = System.currentTimeMillis()

        FriendRelations.insert {
            it[userId] = fromUserId
            it[friendId] = toUserId
            it[status] = FriendRelationStatus.PENDING.name
            it[requestedBy] = fromUserId
            it[createdAt] = now
            it[updatedAt] = now
        }

        true
    }

    override fun updateFriendshipStatus(
        userId: Int,
        targetPhone: String,
        newStatus: FriendRelationStatus
    ): Boolean = transaction {

        val targetUser = Users
            .selectAll()
            .where { Users.phone eq targetPhone }
            .singleOrNull()
            ?: return@transaction false

        val targetUserId = targetUser[Users.id]
        val now = System.currentTimeMillis()


        val updated = FriendRelations.update({
            ((FriendRelations.userId eq userId) and (FriendRelations.friendId eq targetUserId)) or
                    ((FriendRelations.userId eq targetUserId) and (FriendRelations.friendId eq userId))
        }) {
            it[status] = newStatus.name
            it[updatedAt] = now
        }

        updated > 0
    }

    override fun unblockFriend(userId: Int, targetPhone: String): Boolean = transaction {

        val targetUser = Users
            .selectAll()
            .where { Users.phone eq targetPhone }
            .singleOrNull()
            ?: return@transaction false

        val targetUserId = targetUser[Users.id]
        val now = System.currentTimeMillis()

        val updated = FriendRelations.update({
            (((FriendRelations.userId eq userId) and (FriendRelations.friendId eq targetUserId)) or
                    ((FriendRelations.userId eq targetUserId) and (FriendRelations.friendId eq userId))) and
                    (FriendRelations.status eq FriendRelationStatus.BLOCKED.name)
        }) {
            it[status] = FriendRelationStatus.ACCEPTED.name
            it[updatedAt] = now
        }

        updated > 0
    }

    override fun removeFriend(userId: Int, targetPhone: String): Boolean = transaction {

        val targetUser = Users
            .selectAll()
            .where { Users.phone eq targetPhone }
            .singleOrNull()
            ?: return@transaction false

        val targetUserId = targetUser[Users.id]


        val deleted = FriendRelations.deleteWhere {
            ((FriendRelations.userId eq userId) and (FriendRelations.friendId eq targetUserId)) or
                    ((FriendRelations.userId eq targetUserId) and (FriendRelations.friendId eq userId))
        }

        deleted > 0
    }

    override fun getFriendshipStatus(userId: Int, targetPhone: String): FriendInfo? = transaction {

        val result = FriendRelations
            .innerJoin(Users, { FriendRelations.friendId }, { Users.id })
            .selectAll()
            .where {
                (FriendRelations.userId eq userId) and (Users.phone eq targetPhone)
            }
            .singleOrNull()

        if (result != null) {
            return@transaction mapRowToFriendInfo(result, userId, isUserSender = true)
        }


        val reverseResult = FriendRelations
            .innerJoin(Users, { FriendRelations.userId }, { Users.id })
            .selectAll()
            .where {
                (FriendRelations.friendId eq userId) and (Users.phone eq targetPhone)
            }
            .singleOrNull()

        reverseResult?.let {
            mapRowToFriendInfo(it, userId, isUserSender = false)
        }
    }

    private fun mapRowToFriendInfo(
        row: ResultRow,
        currentUserId: Int,
        isUserSender: Boolean
    ): FriendInfo {

        return FriendInfo(
            user = User(
                id = row[Users.id],
                username = row[Users.username],
                phone = row[Users.phone]
            ),
            status = FriendRelationStatus.valueOf(row[FriendRelations.status]),
            requestedBy = row[FriendRelations.requestedBy],
            createdAt = row[FriendRelations.createdAt],
            updatedAt = row[FriendRelations.updatedAt]
        )
    }
}