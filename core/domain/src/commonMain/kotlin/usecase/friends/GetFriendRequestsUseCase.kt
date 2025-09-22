package usecase.friends

import repository.FriendsRepository

class GetFriendRequestsUseCase(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke() = friendsRepository.getFriendRequests()
}
