package repository

import kotlinx.coroutines.flow.Flow
import model.User
import utils.Result

interface FriendsRepository {
    suspend fun getFriends(): Flow<Result<List<User>>>
    suspend fun getFriendRequests(): Flow<Result<List<User>>>
    suspend fun sendFriendRequest(phone: String): Flow<Result<Unit>>
    suspend fun acceptFriendRequest(userId: String): Flow<Result<Unit>>
    suspend fun rejectFriendRequest(userId: String): Flow<Result<Unit>>
    suspend fun removeFriend(userId: String): Flow<Result<Unit>>
}