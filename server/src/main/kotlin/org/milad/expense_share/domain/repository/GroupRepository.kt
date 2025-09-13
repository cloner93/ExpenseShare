package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.Group

interface GroupRepository {
    fun createGroup(ownerId: Int, name: String, memberIds: List<Int>): Group
    fun addUsersToGroup(ownerId: Int, groupId: Int, memberIds: List<Int>): Boolean
    fun getGroupsOfUser(userId: Int): List<Group>
    fun deleteGroup(ownerId: Int, groupId: Int): Boolean
}