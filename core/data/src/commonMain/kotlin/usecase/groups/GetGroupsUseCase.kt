package usecase.groups

import repository.GroupsRepository

class GetGroupsUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke() = groupRepository.getGroups()
}