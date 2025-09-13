package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.FriendRepository

class FriendsService(private val repository: FriendRepository) {

    fun sendRequest(userId: Int, phone: String): Result<String> {
        return if (repository.sendFriendRequest(userId, phone)) {
            Result.success("Friend request sent successfully")
        } else {
            Result.failure(IllegalStateException("User not found or request already exists"))
        }
    }

    fun acceptRequest(userId: Int, phone: String): Result<String> {
        return if (repository.acceptFriendRequest(userId, phone)) {
            Result.success("Friend request accepted")
        } else {
            Result.failure(IllegalStateException("No pending request found"))
        }
    }

    fun rejectRequest(userId: Int, phone: String): Result<String> {
        return if (repository.rejectFriendRequest(userId, phone)) {
            Result.success("Friend request rejected")
        } else {
            Result.failure(IllegalStateException("No pending request found"))
        }
    }

    fun removeFriend(userId: Int, phone: String): Result<String> {
        return if (repository.removeFriend(userId, phone)) {
            Result.success("Friend removed")
        } else {
            Result.failure(IllegalStateException("Friend not found"))
        }
    }

    fun listFriends(userId: Int): List<User> = repository.getFriends(userId)

    fun listRequests(userId: Int): Pair<List<User>, List<User>> =
        repository.getIncomingRequests(userId) to repository.getOutgoingRequests(userId)
}
