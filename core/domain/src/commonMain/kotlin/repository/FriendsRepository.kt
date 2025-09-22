package repository

import kotlinx.coroutines.flow.Flow
import model.User

interface FriendsRepository {
    suspend fun getFriends(): Flow<Result<List<User>>>
    suspend fun getFriendRequests(): Flow<Result<List<User>>>
    suspend fun sendFriendRequest(phone: String): Flow<Result<Unit>>
    suspend fun acceptFriendRequest(phone: String): Flow<Result<Unit>>
    suspend fun rejectFriendRequest(phone: String): Flow<Result<Unit>>
    suspend fun removeFriend(phone: String): Flow<Result<Unit>>
}