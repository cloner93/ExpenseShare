package usecase.groups

import repository.GroupsRepository

class CreateGroupUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke(name: String, memberIds: List<String>) =
        groupRepository.createGroup(name, memberIds)
}