package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.FriendInfo
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.User

interface FriendRepository {
    fun sendFriendRequest(fromId: Int, toPhone: String): Boolean
    fun removeFriend(userId: Int, friendPhone: String): Boolean
    fun getFriends(userId: Int): List<User>
    fun rejectFriendRequest(userId: Int, friendPhone: String): Boolean
    fun acceptFriendRequest(userId: Int, friendPhone: String): Boolean
    fun getIncomingRequests(userId: Int): List<User>
    fun getOutgoingRequests(userId: Int): List<User>

    fun getFriendsWithStatus(userId: Int, status: FriendRelationStatus? = null): List<FriendInfo>
    fun getIncomingRequestsWithStatus(userId: Int): List<FriendInfo>
    fun getOutgoingRequestsWithStatus(userId: Int): List<FriendInfo>
    fun blockFriend(userId: Int, friendPhone: String): Boolean
    fun unblockFriend(userId: Int, friendPhone: String): Boolean
    fun getBlockedFriends(userId: Int): List<FriendInfo>
    fun getFriendsByStatus(userId: Int, status: FriendRelationStatus): List<User>
}