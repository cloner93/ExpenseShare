package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.Group

@Mockable
interface GroupsRepository {
    suspend fun getGroups(): Flow<Result<List<Group>>>
    suspend fun createGroup(name: String, memberIds: List<String>): Flow<Result<Group>>
    suspend fun updateGroupMembers(groupId: String, memberIds: List<String>): Flow<Result<Unit>>
    suspend fun deleteGroup(groupId: String): Flow<Result<Unit>>
}