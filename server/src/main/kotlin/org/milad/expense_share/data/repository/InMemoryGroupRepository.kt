package org.milad.expense_share.data.repository

import org.milad.expense_share.data.db.FakeDatabase.groupMembers
import org.milad.expense_share.data.db.FakeDatabase.groups
import org.milad.expense_share.data.models.Group
import org.milad.expense_share.data.models.GroupMember
import org.milad.expense_share.domain.repository.GroupRepository

class InMemoryGroupRepository : GroupRepository {

    override fun createGroup(
        ownerId: Int,
        name: String,
        memberIds: List<Int>
    ): Group {
        val group = Group(
            id = groups.size + 1,
            name = name,
            ownerId = ownerId
        )

        groups.add(group)

        groupMembers.add(GroupMember(groupId = group.id, userId = ownerId))

        memberIds.forEach { memberId ->
            if (!groupMembers.any { it.groupId == group.id && it.userId == memberId }) {
                groupMembers.add(GroupMember(groupId = group.id, userId = memberId))
            }
        }

        return group
    }

    override fun addUsersToGroup(
        ownerId: Int,
        groupId: Int,
        memberIds: List<Int>
    ): Boolean {
        groups.find { it.id == groupId && it.ownerId == ownerId }
            ?: throw IllegalArgumentException("Group not found or you are not the owner")

        groupMembers.removeIf { it.groupId == groupId }

        groupMembers.add(GroupMember(groupId = groupId, userId = ownerId))

        memberIds.forEach { memberId ->
            if (!groupMembers.any { it.groupId == groupId && it.userId == memberId }) {
                groupMembers.add(GroupMember(groupId = groupId, userId = memberId))
            }
        }

        return true
    }

    override fun getUsersOfGroup(groupId: Int): List<Int> {
        return groupMembers.filter { it.groupId == groupId }.map { it.userId }
    }

    override fun getGroupsOfUser(userId: Int): List<Group> {
        val list = groups.filter { it.ownerId == userId }

        return list
    }

    override fun deleteGroup(ownerId: Int, groupId: Int): Boolean {
        val group = groups.find { it.ownerId == ownerId && it.id == groupId }

        return groups.remove(group)
    }

}