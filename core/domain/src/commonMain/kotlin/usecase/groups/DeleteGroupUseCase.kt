package usecase.groups

import repository.GroupsRepository

class DeleteGroupUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke(groupId: String) =
        groupRepository.deleteGroup(groupId)
}