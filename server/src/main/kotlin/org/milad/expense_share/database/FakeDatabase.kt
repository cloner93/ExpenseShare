package org.milad.expense_share.database

import org.milad.expense_share.JwtConfig
import org.milad.expense_share.model.AuthResponse
import org.milad.expense_share.model.DashboardData
import org.milad.expense_share.model.Group
import org.milad.expense_share.model.User

object FakeDatabase {
    private val users = mutableListOf<Pair<User, String>>()
    private val groups = mutableListOf<Group>()
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
}
