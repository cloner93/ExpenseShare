package org.milad.expense_share.domain.repository

import org.milad.expense_share.presentation.groups.model.UserGroupResponse

interface GroupRepository {
    fun createGroup(ownerId: Int, name: String, memberIds: List<Int>): UserGroupResponse
    fun updateGroupUsers(ownerId: Int, groupId: Int, memberIds: List<Int>): Boolean
    fun getUsersOfGroup(groupId: Int): List<Int>
    fun getGroupsOfUser(userId: Int): List<UserGroupResponse>
    fun deleteGroup(ownerId: Int, groupId: Int): Boolean
}