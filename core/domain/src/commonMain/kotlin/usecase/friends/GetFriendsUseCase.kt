package usecase.friends

import repository.FriendsRepository

class GetFriendsUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke() = friendsRepository.getFriends()
}