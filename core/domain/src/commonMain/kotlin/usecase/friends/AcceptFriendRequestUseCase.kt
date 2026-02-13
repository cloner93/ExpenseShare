package usecase.friends

import kotlinx.coroutines.flow.Flow
import repository.FriendsRepository

class AcceptFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requesterPhone: String): Flow<Result<String>> {
        return friendsRepository.acceptFriendRequest(requesterPhone)
    }
}