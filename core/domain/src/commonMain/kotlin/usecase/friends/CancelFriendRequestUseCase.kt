package usecase.friends

import kotlinx.coroutines.flow.Flow
import repository.FriendsRepository

class CancelFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(targetPhone: String): Flow<Result<String>> {
        return friendsRepository.cancelFriendRequest(targetPhone)
    }
}