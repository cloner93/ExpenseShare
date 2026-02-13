package usecase.friends

import repository.FriendsRepository

class GetAllFriendsUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke() = friendsRepository.getAllFriends()
}