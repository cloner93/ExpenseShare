package usecase.groups

import repository.GroupsRepository

class UpdateGroupMembersUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke(groupId: String, memberIds: List<String>) =
        groupRepository.updateGroupMembers(groupId, memberIds)
}