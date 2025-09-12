package org.milad.expense_share.database

import org.milad.expense_share.database.models.FriendRelation
import org.milad.expense_share.database.models.FriendRelationStatus
import org.milad.expense_share.database.models.Group
import org.milad.expense_share.database.models.GroupMember
import org.milad.expense_share.database.models.Transaction
import org.milad.expense_share.database.models.TransactionStatus
import org.milad.expense_share.database.models.User

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
        Transaction(
            id = 1,
            groupId = 1,
            title = "Hotel Booking",
            amount = 500.0,
            description = "2 nights stay",
            createdBy = 1,
            status = TransactionStatus.APPROVED,
            approvedBy = 1
        ),
        Transaction(
            id = 2,
            groupId = 1,
            title = "Dinner",
            amount = 200.0,
            description = "Kebab restaurant",
            createdBy = 2,
            status = TransactionStatus.PENDING
        ),
        Transaction(
            id = 3,
            groupId = 2,
            title = "Office Pizza",
            amount = 150.0,
            description = "3 pizzas + drinks",
            createdBy = 2,
            status = TransactionStatus.APPROVED,
            approvedBy = 2
        )
    )
}
