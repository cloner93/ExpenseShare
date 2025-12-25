package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.Group

@Mockable
interface GroupsRepository {
    suspend fun getGroups(): Flow<Result<List<Group>>>
    suspend fun createGroup(name: String, memberIds: List<Int>): Flow<Result<Group>>
    suspend fun updateGroupMembers(groupId: String, memberIds: List<Int>): Flow<Result<String>>
    suspend fun deleteGroup(groupId: String): Flow<Result<String>>
}