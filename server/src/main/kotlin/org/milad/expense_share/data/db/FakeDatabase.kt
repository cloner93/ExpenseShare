package org.milad.expense_share.data.db

import org.milad.expense_share.data.models.FriendRelation
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.Group
import org.milad.expense_share.data.models.GroupMember
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.User

object FakeDatabase {
    val users = mutableListOf<Pair<User, String>>(
        User(1, "Milad", "09120000001") to "pass1",
        User(2, "Sara", "09120000002") to "pass2",
        User(3, "Reza", "09120000003") to "pass3",
        User(4, "Niloofar", "09120000004") to "pass4"
    )
    val groups = mutableListOf<Group>(
        Group(id = 1, name = "Trip to North", ownerId = 1),
        Group(id = 2, name = "Work Lunch", ownerId = 2)
    )
    val groupMembers = mutableListOf<GroupMember>(
        GroupMember(groupId = 1, userId = 1),
        GroupMember(groupId = 1, userId = 2),
        GroupMember(groupId = 1, userId = 3),
        GroupMember(groupId = 2, userId = 2),
        GroupMember(groupId = 2, userId = 4)
    )
    val friends = mutableListOf<FriendRelation>(
        FriendRelation(1, 2, FriendRelationStatus.ACCEPTED),
        FriendRelation(1, 3, FriendRelationStatus.ACCEPTED),
        FriendRelation(2, 4, FriendRelationStatus.PENDING)
    )
    val transactions = mutableListOf<Transaction>(

    )
}