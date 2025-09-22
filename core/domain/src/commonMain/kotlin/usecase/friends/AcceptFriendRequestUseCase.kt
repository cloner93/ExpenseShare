package usecase.friends

import repository.FriendsRepository

class AcceptFriendRequestUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke(phone: String) = friendsRepository.acceptFriendRequest(phone)
}