package usecase.friends

import kotlinx.coroutines.flow.Flow
import repository.FriendsRepository

class RejectFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requesterPhone: String): Flow<Result<String>> {
        return friendsRepository.rejectFriendRequest(requesterPhone)
    }
}
