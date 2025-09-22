package usecase.friends

import repository.FriendsRepository

class RemoveFriendUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke(phone: String) = friendsRepository.removeFriend(phone)
}