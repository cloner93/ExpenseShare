package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.FriendInfo

@Mockable
interface FriendsRepository {
    suspend fun getAllFriends(): Flow<Result<List<FriendInfo>>>
    suspend fun getAcceptedFriends(): Flow<Result<List<FriendInfo>>>
    suspend fun getIncomingRequests(): Flow<Result<List<FriendInfo>>>
    suspend fun getOutgoingRequests(): Flow<Result<List<FriendInfo>>>
    suspend fun getBlockedFriends(): Flow<Result<List<FriendInfo>>>

    suspend fun getFriendshipStatus(targetPhone: String): Flow<Result<FriendInfo>>

    suspend fun sendFriendRequest(targetPhone: String): Flow<Result<String>>
    suspend fun acceptFriendRequest(targetPhone: String): Flow<Result<String>>
    suspend fun rejectFriendRequest(targetPhone: String): Flow<Result<String>>

    suspend fun blockFriend(targetPhone: String): Flow<Result<String>>
    suspend fun unblockFriend(targetPhone: String): Flow<Result<String>>

    suspend fun removeFriend(targetPhone: String): Flow<Result<String>>
    suspend fun cancelFriendRequest(targetPhone: String): Flow<Result<String>>
}