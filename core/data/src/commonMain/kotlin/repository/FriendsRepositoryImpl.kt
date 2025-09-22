package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.User


class FriendsRepositoryImpl(private val networkManager: NetworkManager) : FriendsRepository {
    override suspend fun getFriends(): Flow<Result<List<User>>> {
        return networkManager.get<List<User>>("friends")
    }

    override suspend fun getFriendRequests(): Flow<Result<List<User>>> {
        return networkManager.get<List<User>>("/friends/requests")
    }

    override suspend fun sendFriendRequest(phone: String): Flow<Result<Unit>> {
        return networkManager.post<String, Unit>("/friends/request", body = phone)
    }

    override suspend fun acceptFriendRequest(phone: String): Flow<Result<Unit>> {
        return networkManager.post<String, Unit>("/friends/accept", body = phone)
    }

    override suspend fun rejectFriendRequest(phone: String): Flow<Result<Unit>> {
        return networkManager.post<String, Unit>("/friends/reject", body = phone)
    }

    override suspend fun removeFriend(phone: String): Flow<Result<Unit>> {
        return networkManager.delete<Unit>("/friends/$phone")
    }
}