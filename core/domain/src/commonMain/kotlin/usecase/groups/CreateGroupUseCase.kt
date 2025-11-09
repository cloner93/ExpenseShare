package usecase.groups

import repository.GroupsRepository

class CreateGroupUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke(name: String, memberIds: List<Int>) =
        groupRepository.createGroup(name, memberIds)
}