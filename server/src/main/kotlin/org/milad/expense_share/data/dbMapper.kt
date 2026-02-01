package org.milad.expense_share.data

import org.jetbrains.exposed.sql.ResultRow
import org.milad.expense_share.data.db.table.GroupMembers
import org.milad.expense_share.data.db.table.Groups
import org.milad.expense_share.data.db.table.Transactions
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.FriendRelation
import org.milad.expense_share.data.models.Group
import org.milad.expense_share.data.models.GroupMember
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.User

fun ResultRow.toUser() = User(
    id = this[Users.id],
    username = this[Users.username],
    phone = this[Users.phone]
)

fun ResultRow.toFriendRelation() = FriendRelation(
    userId = this[Friends.userId],
    friendId = this[Friends.friendId],
    status = this[Friends.status]
)

fun ResultRow.toGroup() = Group(
    id = this[Groups.id],
    name = this[Groups.name],
    ownerId = this[Groups.ownerId]
)

fun ResultRow.toGroupMember() = GroupMember(
    groupId = this[GroupMembers.groupId],
    userId = this[GroupMembers.userId]
)

fun ResultRow.toTransaction() = Transaction(
    id = this[Transactions.id],
    groupId = this[Transactions.groupId],
    title = this[Transactions.title],
    amount = this[Transactions.amount],
    description = this[Transactions.description],
    createdBy = this[Transactions.createdBy],
    status = this[Transactions.status],
    createdAt = this[Transactions.createdAt],
    transactionDate = this[Transactions.transactionDate],
    approvedBy = this[Transactions.approvedBy]
)
