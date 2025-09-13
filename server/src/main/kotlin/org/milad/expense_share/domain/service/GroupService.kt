package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.Group
import org.milad.expense_share.domain.repository.GroupRepository

class GroupService(private val groupRepository: GroupRepository) {

    fun createGroup(ownerId: Int, name: String, members: List<Int>): Result<Group> {
        return try {
            val group = groupRepository.createGroup(ownerId, name, members)
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserGroups(userId: Int): List<Group> =
        groupRepository.getGroupsOfUser(userId)

    fun addUsers(userId: Int, groupId: Int, memberIds: List<Int>): Result<String> {
        return if (groupRepository.addUsersToGroup(userId, groupId, memberIds)) {
            Result.success("Users added successfully")
        } else {
            Result.failure(IllegalAccessException("Only group owner can add members"))
        }
    }

    fun deleteGroup(userId: Int, groupId: Int): Result<String> {
        return if (groupRepository.deleteGroup(userId, groupId)) {
            Result.success("Group deleted successfully")
        } else {
            Result.failure(IllegalAccessException("Only group owner can delete the group"))
        }
    }
}
