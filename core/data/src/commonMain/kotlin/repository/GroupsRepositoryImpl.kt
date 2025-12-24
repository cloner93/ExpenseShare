package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.CreateGroupRequest
import model.Group


class GroupsRepositoryImpl(private val networkManager: NetworkManager) : GroupsRepository {
    override suspend fun getGroups(): Flow<Result<List<Group>>> {
        return networkManager.get<List<Group>>("/groups")
    }

    override suspend fun createGroup(
        name: String,
        memberIds: List<Int>
    ): Flow<Result<Group>> {
        return networkManager.post("/groups/create", body = CreateGroupRequest(name, memberIds))
    }

    override suspend fun updateGroupMembers(
        groupId: String,
        memberIds: List<String>
    ): Flow<Result<Unit>> {
        return networkManager.put("/groups/$groupId/updateMembers", body = memberIds)
    }

    override suspend fun deleteGroup(groupId: String): Flow<Result<String>> {
        return networkManager.delete("/groups/$groupId")
    }
}