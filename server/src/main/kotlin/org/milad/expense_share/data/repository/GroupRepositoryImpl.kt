package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.GroupMembers
import org.milad.expense_share.data.db.table.Groups
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.presentation.groups.model.UserGroupResponse

class GroupRepositoryImpl : GroupRepository {

    override fun createGroup(
        ownerId: Int,
        name: String,
        memberIds: List<Int>,
    ): UserGroupResponse = transaction {
        val id = Groups.insert {
            it[Groups.name] = name
            it[Groups.ownerId] = ownerId
        } get Groups.id

        GroupMembers.insert {
            it[groupId] = id
            it[userId] = ownerId
        }

        memberIds.distinct().forEach { memberId ->
            if (!GroupMembers.select((GroupMembers.groupId eq id) and (GroupMembers.userId eq memberId) )
                    .any()
            ) {
                GroupMembers.insert {
                    it[GroupMembers.groupId] = groupId
                    it[GroupMembers.userId] = memberId
                }
            }
        }

        UserGroupResponse(
            id = id,
            name = name,
            ownerId = ownerId
        )
    }

    override fun addUsersToGroup(ownerId: Int, groupId: Int, memberIds: List<Int>): Boolean =
        transaction {
            val group = Groups.selectAll()
                .where { (Groups.id eq groupId) and (Groups.ownerId eq ownerId) }
                .singleOrNull()
                ?: throw IllegalArgumentException("Group not found or you are not the owner")

            GroupMembers.deleteWhere { GroupMembers.groupId eq groupId }

            // add back owner + members
            val allMembers = memberIds.distinct() + ownerId
            allMembers.forEach { memberId ->
                GroupMembers.insert {
                    it[GroupMembers.groupId] = groupId
                    it[GroupMembers.userId] = memberId
                }
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
        val deleted = Groups.deleteWhere {
            (Groups.id eq groupId) and (Groups.ownerId eq ownerId)
        }
        if (deleted > 0) {
            GroupMembers.deleteWhere { GroupMembers.groupId eq groupId }
            true
        } else false
    }
}
