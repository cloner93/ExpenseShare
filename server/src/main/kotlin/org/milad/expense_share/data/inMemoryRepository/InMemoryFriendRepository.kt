package org.milad.expense_share.data.inMemoryRepository

import org.milad.expense_share.data.db.FakeDatabase.friends
import org.milad.expense_share.data.db.FakeDatabase.users
import org.milad.expense_share.data.models.FriendRelation
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.FriendRepository

class InMemoryFriendRepository : FriendRepository {

    fun getUserByPhone(phone: String): User? {
        return users.find { it.first.phone == phone }?.first
    }

    override fun sendFriendRequest(fromId: Int, toPhone: String): Boolean {
        val toUser = getUserByPhone(toPhone) ?: return false
        if (friends.any { it.userId == fromId && it.friendId == toUser.id }) return false
        friends.add(FriendRelation(fromId, toUser.id, FriendRelationStatus.PENDING))
        return true
    }

    override fun removeFriend(userId: Int, friendPhone: String): Boolean {
        val friendUser = getUserByPhone(friendPhone) ?: return false
        return friends.removeIf {
            (it.userId == userId && it.friendId == friendUser.id) ||
                    (it.friendId == userId && it.userId == friendUser.id)
        }
    }

    override fun getFriends(userId: Int): List<User> {
        val friendIds = friends.filter {
            (it.userId == userId || it.friendId == userId) && it.status == FriendRelationStatus.ACCEPTED
        }.map { if (it.userId == userId) it.friendId else it.userId }
        return users.map { it.first }.filter { it.id in friendIds }
    }

    override fun rejectFriendRequest(
        userId: Int,
        friendPhone: String,
    ): Boolean {
        val friendUser = getUserByPhone(friendPhone) ?: return false
        val relation =
            friends.find { it.userId == friendUser.id && it.friendId == userId && it.status == FriendRelationStatus.PENDING }
        return if (relation != null) {
            relation.status = FriendRelationStatus.REJECTED
            true
        } else false
    }

    override fun acceptFriendRequest(
        userId: Int,
        friendPhone: String,
    ): Boolean {
        val friendUser = getUserByPhone(friendPhone) ?: return false
        val relation =
            friends.find { it.userId == friendUser.id && it.friendId == userId && it.status == FriendRelationStatus.PENDING }
        return if (relation != null) {
            relation.status = FriendRelationStatus.ACCEPTED
            true
        } else false
    }

    override fun getIncomingRequests(userId: Int): List<User> {
        val incomingIds =
            friends.filter { it.friendId == userId && it.status == FriendRelationStatus.PENDING }
                .map { it.userId }
        return users.map { it.first }.filter { it.id in incomingIds }
    }

    override fun getOutgoingRequests(userId: Int): List<User> {
        val outgoingIds =
            friends.filter { it.userId == userId && it.status == FriendRelationStatus.PENDING }
                .map { it.friendId }
        return users.map { it.first }.filter { it.id in outgoingIds }
    }
}