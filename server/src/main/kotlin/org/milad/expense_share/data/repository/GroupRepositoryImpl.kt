package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.GroupMembers
import org.milad.expense_share.data.db.table.Groups
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.presentation.groups.model.UserGroupResponse

class GroupRepositoryImpl : GroupRepository {

    override fun createGroup(
        ownerId: Int,
        name: String,
        memberIds: List<Int>,
    ): UserGroupResponse = transaction {
        val uniqueMemberIds = (memberIds + ownerId).distinct()

        val groupId = Groups.insert {
            it[Groups.name] = name
            it[Groups.ownerId] = ownerId
        } get Groups.id

        if (uniqueMemberIds.isNotEmpty()) {
            GroupMembers.batchInsert(uniqueMemberIds) { memberId ->
                this[GroupMembers.groupId] = groupId
                this[GroupMembers.userId] = memberId
            }
        }

        val members = if (uniqueMemberIds.isNotEmpty()) {
            Users
                .selectAll()
                .where { Users.id inList uniqueMemberIds }
                .map {
                    User(
                        id = it[Users.id],
                        username = it[Users.username],
                        phone = it[Users.phone]
                    )
                }
        } else {
            emptyList()
        }

        UserGroupResponse(
            id = groupId,
            name = name,
            ownerId = ownerId,
            members = members,
            transactions = emptyList()
        )
    }


    override fun updateGroupUsers(
        ownerId: Int,
        groupId: Int,
        memberIds: List<Int>
    ): Boolean = transaction {

        val groupExists = Groups
            .selectAll().where { (Groups.id eq groupId) and (Groups.ownerId eq ownerId) }
            .any()

        if (!groupExists) return@transaction false

        val allMembers = (memberIds + ownerId).distinct()

        val validUserIds = Users
            .selectAll().where { Users.id inList allMembers }
            .map { it[Users.id] }
            .toSet()

        if (validUserIds.size != allMembers.size) return@transaction false

        GroupMembers.deleteWhere { GroupMembers.groupId eq groupId }

        GroupMembers.batchInsert(validUserIds) { userId ->
            this[GroupMembers.groupId] = groupId
            this[GroupMembers.userId] = userId
        }

        true
    }


    override fun getUsersOfGroup(groupId: Int): List<Int> = transaction {
        GroupMembers.selectAll().where { GroupMembers.groupId eq groupId }
            .map { it[GroupMembers.userId] }
    }

    override fun getGroupsOfUser(userId: Int): List<UserGroupResponse> = transaction {
        (Groups innerJoin GroupMembers)
//            .slice(Groups.id, Groups.name, Groups.ownerId)
            .selectAll()
            .where { GroupMembers.userId eq userId }
            .map {
                UserGroupResponse(
                    id = it[Groups.id],
                    name = it[Groups.name],
                    ownerId = it[Groups.ownerId]
                )
            }
    }

    override fun deleteGroup(ownerId: Int, groupId: Int): Boolean = transaction {
        Groups.deleteWhere {
            (Groups.id eq groupId) and (Groups.ownerId eq ownerId)
        } > 0
    }
}
