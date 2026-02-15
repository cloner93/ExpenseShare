package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.FriendInfo
import model.FriendRequest

class FriendsRepositoryImpl(private val networkManager: NetworkManager) : FriendsRepository {
    override suspend fun getAllFriends(): Flow<Result<List<FriendInfo>>> {
        return networkManager.get<List<FriendInfo>>("/friends")
    }

    override suspend fun getAcceptedFriends(): Flow<Result<List<FriendInfo>>> {
        return networkManager.get<List<FriendInfo>>("/friends/accepted")
    }

    override suspend fun getIncomingRequests(): Flow<Result<List<FriendInfo>>> {
        return networkManager.get<List<FriendInfo>>("/friends/requests/incoming")
    }

    override suspend fun getOutgoingRequests(): Flow<Result<List<FriendInfo>>> {
        return networkManager.get<List<FriendInfo>>("/friends/requests/outgoing")
    }

    override suspend fun getBlockedFriends(): Flow<Result<List<FriendInfo>>> {
        return networkManager.get<List<FriendInfo>>("/friends/blocked")
    }

    override suspend fun getFriendshipStatus(targetPhone: String): Flow<Result<FriendInfo>> {
        return networkManager.get<FriendInfo>("/friends/status/{$targetPhone}")
    }

    override suspend fun sendFriendRequest(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)
        return networkManager.post<FriendRequest, String>("/friends/request", body = request)
    }

    override suspend fun acceptFriendRequest(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)
        return networkManager.put<FriendRequest, String>("/friends/accept", body = request)
    }

    override suspend fun rejectFriendRequest(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)
        return networkManager.put<FriendRequest, String>("/friends/reject", body = request)
    }

    override suspend fun blockFriend(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)
        return networkManager.put<FriendRequest, String>("/friends/block", body = request)
    }

    override suspend fun unblockFriend(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)
        return networkManager.put<FriendRequest, String>("/friends/unblock", body = request)
    }

    override suspend fun removeFriend(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)
        return networkManager.put<FriendRequest, String>("/friends/remove", body = request)
    }

    override suspend fun cancelFriendRequest(targetPhone: String): Flow<Result<String>> {
        val request = FriendRequest(targetPhone)

        return networkManager.put<FriendRequest, String>("/friends/cancel", body = request)
    }
}