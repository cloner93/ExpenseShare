package org.milad.expense_share.database

import org.milad.expense_share.JwtConfig
import org.milad.expense_share.model.AuthResponse
import org.milad.expense_share.model.DashboardData
import org.milad.expense_share.model.FriendRelation
import org.milad.expense_share.model.Group
import org.milad.expense_share.model.User

object FakeDatabase {
    private val users = mutableListOf<Pair<User, String>>(
        User(1, "Alice", "1234567890") to "password123",
        User(2, "Bob", "0987654321") to "securepass",
        User(3, "Charlie", "1122334455") to "charliepass",
        User(4, "cloner93", "09137511005") to "12345"
    )
    private val groups = mutableListOf<Group>(
        Group(
            1,
            "Trip to Berlin",
            1,
            listOf(users[0].first, users[1].first, users[2].first),
            50.0,
            20.0
        ),
        Group(2, "Dinner with Friends", 2, listOf(users[1].first, users[2].first), 15.0, 0.0),
        Group(3, "Roommates", 1, listOf(users[0].first, users[2].first), 0.0, 30.0)
    )
    private val friends = mutableListOf<FriendRelation>(
        FriendRelation(1, 2, "accepted"),
        FriendRelation(1, 3, "pending"),
        FriendRelation(2, 3, "rejected")
    )
    private var lastId = 0

    fun register(username: String, phone: String, password: String): AuthResponse {
        if (users.any { it.first.phone == phone }) {
            return AuthResponse(false, "Phone already registered")
        }
        val user = User(++lastId, username, phone)
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

    fun createGroup(ownerId: Int, name: String): Group {
        val group = Group(groups.size + 1, name, ownerId)
        groups.add(group)
        return group
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
}
