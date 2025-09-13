package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.Group
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.presentation.groups.model.UserGroupResponse

class GroupService(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository
) {

    fun createGroup(ownerId: Int, name: String, members: List<Int>): Result<Group> {
        return try {
            val group = groupRepository.createGroup(ownerId, name, members)
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserGroups(userId: Int): Result<MutableList<UserGroupResponse>> {
        try {
            val listOfGroup = groupRepository.getGroupsOfUser(userId)
            val listOfUserGroupResponse = mutableListOf<UserGroupResponse>()

            listOfGroup.forEach { group ->
                val transactions = transactionRepository.getTransactions(userId, group.id)
                val members = getUsersOfGroup(group.id)

                val userGroup = UserGroupResponse(
                    id = group.id,
                    name = group.name,
                    ownerId = group.ownerId,
                    members = members,
                    transactions = transactions
                )

                listOfUserGroupResponse.add(userGroup)
            }
            return Result.success(listOfUserGroupResponse)

        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun getUsersOfGroup(groupId: Int): List<User> {
        val userIds = groupRepository.getUsersOfGroup(groupId)
        val users = mutableListOf<User>()
        userIds.forEach { userId ->
            val user = userRepository.findById(userId)
            if (user != null) {
                users.add(user)
            }
        }
        return users
    }

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
