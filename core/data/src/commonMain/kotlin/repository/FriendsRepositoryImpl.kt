package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.FriendInfo

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
        return networkManager.post<String, String>("/friends/request", body = targetPhone)
    }

    override suspend fun acceptFriendRequest(targetPhone: String): Flow<Result<String>> {
        return networkManager.put<String, String>("/friends/accept", body = targetPhone)
    }

    override suspend fun rejectFriendRequest(targetPhone: String): Flow<Result<String>> {
        return networkManager.put<String, String>("/friends/reject", body = targetPhone)
    }

    override suspend fun blockFriend(targetPhone: String): Flow<Result<String>> {
        return networkManager.put<String, String>("/friends/block", body = targetPhone)
    }

    override suspend fun unblockFriend(targetPhone: String): Flow<Result<String>> {
        return networkManager.put<String, String>("/friends/unblock", body = targetPhone)
    }

    override suspend fun removeFriend(targetPhone: String): Flow<Result<String>> {
        return networkManager.put<String, String>("/friends/remove", body = targetPhone)
    }

    override suspend fun cancelFriendRequest(targetPhone: String): Flow<Result<String>> {
        return networkManager.put<String, String>("/friends/cancel", body = targetPhone)
    }
}