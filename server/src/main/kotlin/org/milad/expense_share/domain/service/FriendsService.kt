package org.milad.expense_share.domain.service

import org.milad.expense_share.data.models.FriendInfo
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.domain.repository.FriendRepository
import org.milad.expense_share.domain.repository.UserRepository

class FriendsService(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) {
    fun getAllFriends(userId: Int, status: FriendRelationStatus? = null): Result<List<FriendInfo>> {
        return try {
            val friends = friendRepository.getAllFriends(userId, status)
            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch friends: ${e.message}", e))
        }
    }

    fun getAcceptedFriends(userId: Int): Result<List<FriendInfo>> {
        return try {
            val friends = friendRepository.getAcceptedFriends(userId)
            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch accepted friends: ${e.message}", e))
        }
    }

    fun getIncomingRequests(userId: Int): Result<List<FriendInfo>> {
        return try {
            val requests = friendRepository.getIncomingRequests(userId)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch incoming requests: ${e.message}", e))
        }
    }

    fun getOutgoingRequests(userId: Int): Result<List<FriendInfo>> {
        return try {
            val requests = friendRepository.getOutgoingRequests(userId)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch outgoing requests: ${e.message}", e))
        }
    }

    fun getBlockedFriends(userId: Int): Result<List<FriendInfo>> {
        return try {
            val blocked = friendRepository.getBlockedFriends(userId)
            Result.success(blocked)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch blocked friends: ${e.message}", e))
        }
    }

    fun getFriendshipStatus(userId: Int, targetPhone: String): Result<FriendInfo?> {
        return try {
            val status = friendRepository.getFriendshipStatus(userId, targetPhone)
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get friendship status: ${e.message}", e))
        }
    }

    fun sendFriendRequest(fromUserId: Int, toUserPhone: String): Result<String> {
        return try {
            val user = userRepository.findById(fromUserId)
            user?.let {
                if (it.id == fromUserId)
                    return Result.failure(Exception("You are always your own friend."))
            }

            val existingStatus = friendRepository.getFriendshipStatus(fromUserId, toUserPhone)

            if (existingStatus != null) {
                return Result.failure(
                    when (existingStatus.status) {
                        FriendRelationStatus.ACCEPTED ->
                            Exception("You are already friends with this user")

                        FriendRelationStatus.PENDING ->
                            Exception("Friend request already sent")

                        FriendRelationStatus.BLOCKED ->
                            Exception("Cannot send request to blocked user")

                        FriendRelationStatus.REJECTED ->
                            Exception("Previous request was rejected. Please wait before sending again")
                    }
                )
            }

            val success = friendRepository.sendFriendRequest(fromUserId, toUserPhone)
            if (success) {
                Result.success("Friend request sent successfully")
            } else {
                Result.failure(Exception("User not found or request failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send friend request: ${e.message}", e))
        }
    }

    fun acceptFriendRequest(userId: Int, requesterPhone: String): Result<String> {
        return try {

            val friendship = friendRepository.getFriendshipStatus(userId, requesterPhone)

            if (friendship == null) {
                return Result.failure(Exception("No friend request found from this user"))
            }

            if (friendship.status != FriendRelationStatus.PENDING) {
                return Result.failure(
                    Exception("Cannot accept: Request status is ${friendship.status}")
                )
            }


            if (friendship.requestedBy == userId) {
                return Result.failure(
                    Exception("Cannot accept your own friend request. Wait for them to respond.")
                )
            }


            val success = friendRepository.acceptFriendRequest(userId, requesterPhone)
            if (success) {
                Result.success("Friend request accepted successfully")
            } else {
                Result.failure(Exception("Failed to accept friend request"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to accept friend request: ${e.message}", e))
        }
    }

    fun rejectFriendRequest(userId: Int, requesterPhone: String): Result<String> {
        return try {

            val friendship = friendRepository.getFriendshipStatus(userId, requesterPhone)

            if (friendship == null) {
                return Result.failure(Exception("No friend request found from this user"))
            }

            if (friendship.status != FriendRelationStatus.PENDING) {
                return Result.failure(
                    Exception("Cannot reject: Request status is ${friendship.status}")
                )
            }


            if (friendship.requestedBy == userId) {
                return Result.failure(
                    Exception("Cannot reject your own request. You can cancel it instead.")
                )
            }


            val success = friendRepository.rejectFriendRequest(userId, requesterPhone)
            if (success) {
                Result.success("Friend request rejected successfully")
            } else {
                Result.failure(Exception("Failed to reject friend request"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reject friend request: ${e.message}", e))
        }
    }

    fun blockFriend(userId: Int, targetPhone: String): Result<String> {
        return try {
            val success = friendRepository.blockFriend(userId, targetPhone)
            if (success) {
                Result.success("User blocked successfully")
            } else {
                Result.failure(Exception("User not found or already blocked"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to block user: ${e.message}", e))
        }
    }

    fun unblockFriend(userId: Int, targetPhone: String): Result<String> {
        return try {

            val friendship = friendRepository.getFriendshipStatus(userId, targetPhone)

            if (friendship == null) {
                return Result.failure(Exception("No relationship found with this user"))
            }

            if (friendship.status != FriendRelationStatus.BLOCKED) {
                return Result.failure(Exception("User is not blocked"))
            }

            val success = friendRepository.blockFriend(userId, targetPhone)
            if (success) {
                Result.success("User unblocked successfully")
            } else {
                Result.failure(Exception("Failed to unblock user"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to unblock user: ${e.message}", e))
        }
    }

    fun removeFriend(userId: Int, targetPhone: String): Result<String> {
        return try {

            val friendship = friendRepository.getFriendshipStatus(userId, targetPhone)

            if (friendship == null) {
                return Result.failure(Exception("No friendship found with this user"))
            }

            val success = friendRepository.removeFriend(userId, targetPhone)
            if (success) {
                Result.success("Friend removed successfully")
            } else {
                Result.failure(Exception("Failed to remove friend"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to remove friend: ${e.message}", e))
        }
    }

    fun cancelFriendRequest(userId: Int, targetPhone: String): Result<String> {
        return try {

            val friendship = friendRepository.getFriendshipStatus(userId, targetPhone)

            if (friendship == null) {
                return Result.failure(Exception("No friend request found"))
            }

            if (friendship.status != FriendRelationStatus.PENDING) {
                return Result.failure(
                    Exception("Cannot cancel: Request status is ${friendship.status}")
                )
            }

            if (friendship.requestedBy != userId) {
                return Result.failure(
                    Exception("Cannot cancel incoming requests. Use reject instead.")
                )
            }


            val success = friendRepository.removeFriend(userId, targetPhone)
            if (success) {
                Result.success("Friend request cancelled successfully")
            } else {
                Result.failure(Exception("Failed to cancel request"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to cancel friend request: ${e.message}", e))
        }
    }
}