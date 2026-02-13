package usecase.friends

import kotlinx.coroutines.flow.Flow
import repository.FriendsRepository

class SendFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(targetPhone: String): Flow<Result<String>> {
        return friendsRepository.sendFriendRequest(targetPhone)
    }
}