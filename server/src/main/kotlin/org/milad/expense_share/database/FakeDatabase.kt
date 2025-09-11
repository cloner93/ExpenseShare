package org.milad.expense_share.database

import org.milad.expense_share.JwtConfig
import org.milad.expense_share.model.AuthResponse
import org.milad.expense_share.model.DashboardData
import org.milad.expense_share.model.FriendRelation
import org.milad.expense_share.model.Group
import org.milad.expense_share.model.GroupMember
import org.milad.expense_share.model.Transaction
import org.milad.expense_share.model.TransactionStatus
import org.milad.expense_share.model.User

object FakeDatabase {
    private val users = mutableListOf<Pair<User, String>>(
        User(1, "Milad", "09120000001") to "pass1",
        User(2, "Sara", "09120000002") to "pass2",
        User(3, "Reza", "09120000003") to "pass3",
        User(4, "Niloofar", "09120000004") to "pass4"
    )
    private val groups = mutableListOf<Group>(
        Group(id = 1, name = "Trip to North", ownerId = 1),
        Group(id = 2, name = "Work Lunch", ownerId = 2)
    )
    private val groupMembers = mutableListOf<GroupMember>(
        GroupMember(groupId = 1, userId = 1),
        GroupMember(groupId = 1, userId = 2),
        GroupMember(groupId = 1, userId = 3),
        GroupMember(groupId = 2, userId = 2),
        GroupMember(groupId = 2, userId = 4)
    )
    private val friends = mutableListOf<FriendRelation>(
        FriendRelation(1, 2, "accepted"),
        FriendRelation(1, 3, "accepted"),
        FriendRelation(2, 4, "pending")
    )
    private val transactions = mutableListOf<Transaction>(
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

    fun register(username: String, phone: String, password: String): AuthResponse {
        if (users.any { it.first.phone == phone }) {
            return AuthResponse(false, "Phone already registered")
        }
        val user = User(users.size + 1, username, phone)
        users.add(user to password)

        val token = JwtConfig.generateToken(user)

        return AuthResponse(
            true, "Registered successfully", token, user
        )
    }

    fun login(phone: String, password: String): AuthResponse {
        val match = users.find { it.first.phone == phone && it.second == password }
        return if (match != null) {
            val token = JwtConfig.generateToken(match.first)
            AuthResponse(true, "Login successful", token, match.first)
        } else {
            AuthResponse(false, "Invalid phone or password")
        }
    }

    fun createGroup(ownerId: Int, name: String, memberIds: List<Int>): Group {
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

    fun addUserToGroup(ownerId: Int, groupId: Int, memberIds: List<Int>): Boolean {
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

    fun getGroupsOfUser(userId: Int): DashboardData {
        val list = groups.filter { it.ownerId == userId }
        var totalDebt = 0.0
        var totalCredit = 0.0

        list.forEach {
            totalDebt += it.totalDebt
            totalCredit += it.totalCredit
        }

        return DashboardData(list, totalDebt, totalCredit)
    }

    fun getUserByPhone(phone: String): User? {
        return users.find { it.first.phone == phone }?.first
    }

    fun sendFriendRequest(fromId: Int, toPhone: String): Boolean {
        val toUser = getUserByPhone(toPhone) ?: return false
        if (friends.any { it.userId == fromId && it.friendId == toUser.id }) return false
        friends.add(FriendRelation(fromId, toUser.id, "pending"))
        return true
    }

    fun getIncomingRequests(userId: Int): List<User> {
        val incomingIds = friends.filter { it.friendId == userId && it.status == "pending" }
            .map { it.userId }
        return users.map { it.first }.filter { it.id in incomingIds }
    }

    fun getOutgoingRequests(userId: Int): List<User> {
        val outgoingIds = friends.filter { it.userId == userId && it.status == "pending" }
            .map { it.friendId }
        return users.map { it.first }.filter { it.id in outgoingIds }
    }

    fun acceptFriendRequest(userId: Int, friendPhone: String): Boolean {
        val friendUser = getUserByPhone(friendPhone) ?: return false
        val relation =
            friends.find { it.userId == friendUser.id && it.friendId == userId && it.status == "pending" }
        return if (relation != null) {
            relation.status = "accepted"
            true
        } else false
    }

    fun rejectFriendRequest(userId: Int, friendPhone: String): Boolean {
        val friendUser = getUserByPhone(friendPhone) ?: return false
        val relation =
            friends.find { it.userId == friendUser.id && it.friendId == userId && it.status == "pending" }
        return if (relation != null) {
            relation.status = "rejected"
            true
        } else false
    }

    fun getFriends(userId: Int): List<User> {
        val friendIds = friends.filter {
            (it.userId == userId || it.friendId == userId) && it.status == "accepted"
        }.map { if (it.userId == userId) it.friendId else it.userId }
        return users.map { it.first }.filter { it.id in friendIds }
    }

    fun removeFriend(userId: Int, friendPhone: String): Boolean {
        val friendUser = getUserByPhone(friendPhone) ?: return false
        return friends.removeIf {
            (it.userId == userId && it.friendId == friendUser.id) ||
                    (it.friendId == userId && it.userId == friendUser.id)
        }
    }

    fun removeGroup(ownerId: Int, groupId: Int): Boolean {
        val group = groups.find { it.ownerId == ownerId && it.id == groupId }

        return groups.remove(group)
    }

    fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Double,
        description: String
    ): Transaction? {
        val group = groups.find { it.id == groupId } ?: return null

        val status = if (group.ownerId == userId) {
            TransactionStatus.APPROVED
        } else {
            TransactionStatus.PENDING
        }

        val tx = Transaction(
            id = transactions.size + 1,
            groupId = groupId,
            title = title,
            amount = amount,
            description = description,
            createdBy = userId,
            status = status,
            approvedBy = if (status == TransactionStatus.APPROVED) userId else null
        )
        transactions.add(tx)
        return tx
    }

    fun getTransactions(userId: Int, groupId: Int): List<Transaction> {
        val group = groups.find { it.id == groupId } ?: return emptyList()

        return if (group.ownerId == userId) {
            transactions.filter { it.groupId == groupId }
        } else {
            transactions.filter {
                it.groupId == groupId &&
                        (it.status == TransactionStatus.APPROVED ||
                                (it.status == TransactionStatus.PENDING && it.createdBy == userId))
            }
        }
    }

    fun approveTransaction(transactionId: Int, managerId: Int): Boolean {
        val tx = transactions.find { it.id == transactionId } ?: return false
        val group = groups.find { it.id == tx.groupId } ?: return false

        if (group.ownerId != managerId) return false

        tx.status = TransactionStatus.APPROVED
        tx.approvedBy = managerId
        return true
    }

    fun rejectTransaction(transactionId: Int, managerId: Int): Boolean {
        val tx = transactions.find { it.id == transactionId } ?: return false
        val group = groups.find { it.id == tx.groupId } ?: return false

        if (group.ownerId != managerId) return false

        tx.status = TransactionStatus.REJECTED
        tx.approvedBy = managerId
        return true
    }

    fun deleteTransaction(transactionId: Int, managerId: Int): Boolean {
        val tx = transactions.find { it.id == transactionId } ?: return false
        val group = groups.find { it.id == tx.groupId } ?: return false

        if (group.ownerId != managerId) return false

        transactions.remove(tx)
        return true
    }
}
