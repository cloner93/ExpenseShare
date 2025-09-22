package usecase.groups

import repository.GroupsRepository

class UpdateGroupMembersUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke(name: String, memberIds: List<String>) =
        groupRepository.updateGroupMembers(name, memberIds)
}