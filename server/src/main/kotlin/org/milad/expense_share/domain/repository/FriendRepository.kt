package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.User

interface FriendRepository {
    fun sendFriendRequest(fromId: Int, toPhone: String): Boolean
    fun removeFriend(userId: Int, friendPhone: String): Boolean
    fun getFriends(userId: Int): List<User>
    fun rejectFriendRequest(userId: Int, friendPhone: String): Boolean
    fun acceptFriendRequest(userId: Int, friendPhone: String): Boolean
    fun getIncomingRequests(userId: Int): List<User>
    fun getOutgoingRequests(userId: Int): List<User>
}