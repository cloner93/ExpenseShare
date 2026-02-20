package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.FriendInfo
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.domain.model.FriendDirection

interface FriendRepository {
    fun getAllFriends(
        userId: Int,
        status: FriendRelationStatus? = null,
        direction: FriendDirection? = null
    ): List<FriendInfo>

    fun getAcceptedFriends(userId: Int) = getAllFriends(userId, FriendRelationStatus.ACCEPTED)
    fun getIncomingRequests(userId: Int) =
        getAllFriends(userId, FriendRelationStatus.PENDING, FriendDirection.INCOMING)

    fun getOutgoingRequests(userId: Int) =
        getAllFriends(userId, FriendRelationStatus.PENDING, FriendDirection.OUTGOING)

    fun getBlockedFriends(userId: Int) = getAllFriends(userId, FriendRelationStatus.BLOCKED)

    fun sendFriendRequest(fromUserId: Int, toUserPhone: String): Boolean

    fun updateFriendshipStatus(
        userId: Int,
        targetPhone: String,
        newStatus: FriendRelationStatus
    ): Boolean

    fun acceptFriendRequest(userId: Int, requesterPhone: String): Boolean =
        updateFriendshipStatus(userId, requesterPhone, FriendRelationStatus.ACCEPTED)

    fun rejectFriendRequest(userId: Int, requesterPhone: String): Boolean =
        updateFriendshipStatus(userId, requesterPhone, FriendRelationStatus.REJECTED)

    fun blockFriend(userId: Int, targetPhone: String): Boolean =
        updateFriendshipStatus(userId, targetPhone, FriendRelationStatus.BLOCKED)

    fun unblockFriend(userId: Int, targetPhone: String): Boolean

    fun removeFriend(userId: Int, targetPhone: String): Boolean
    fun getFriendshipStatus(userId: Int, targetPhone: String): FriendInfo?
    fun areFriends(userId: Int, targetPhone: String): Boolean {
        return getFriendshipStatus(userId, targetPhone)?.status == FriendRelationStatus.ACCEPTED
    }
}