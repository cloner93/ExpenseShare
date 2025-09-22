package usecase.friends

import repository.FriendsRepository

class SendFriendRequestUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke(phone: String) = friendsRepository.sendFriendRequest(phone)
}