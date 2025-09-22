package usecase.friends

import repository.FriendsRepository

class RejectFriendRequestUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke(phone: String) = friendsRepository.rejectFriendRequest(phone)
}